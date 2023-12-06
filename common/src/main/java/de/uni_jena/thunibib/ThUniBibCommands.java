package de.uni_jena.thunibib;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.commands.MCRSolrCommands;
import org.mycore.ubo.ldap.LDAPAuthenticator;
import org.mycore.ubo.ldap.LDAPObject;
import org.mycore.ubo.ldap.LDAPSearcher;
import org.mycore.user2.MCRRealm;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mycore.common.MCRConstants.*;

@MCRCommandGroup(name = "ThUniBib Tools")

public class ThUniBibCommands {
    private static final Logger LOGGER = LogManager.getLogger(ThUniBibCommands.class);

    private static List<String> DEFAULT_ROLES = List.of("submitter", "admin");

    private static final String LDAP_ID_NAME = "id_" + MCRConfiguration2.getString("UBO.projectid.default").get();

    private static final String FUNDING_SEPARATOR_CHAR = "#";

    @MCRCommand(syntax = "thunibib update object {0} with fundings {1}",
        help =
            "Updates the funding of of the given object in {0} with the fundings in {1}. Multiple fundings should be separated by "
                + ThUniBibCommands.FUNDING_SEPARATOR_CHAR)
    public static void updateFundingForObject(String mcrid, String fundings) {
        LOGGER.info("Updating funding of {} with {}", mcrid, fundings);

        MCRObjectID mcrObjectID = MCRObjectID.getInstance(mcrid);
        Document mcrObject = MCRMetadataManager.retrieveMCRObject(mcrObjectID).createXML();
        List<String> fundingList = Arrays.asList(fundings.split(ThUniBibCommands.FUNDING_SEPARATOR_CHAR));

        if (!ThUniBibCommands.fundingChanged(mcrObject, fundingList)) {
            LOGGER.info("The funding information of {} did not change, nothing to do", mcrObjectID);
            return;
        }

        // check validity of category id
        for (String fundingId : fundingList) {
            if (!MCRXMLFunctions.isCategoryID("fundingType", fundingId)) {
                LOGGER.error("fundingType:{} is not a valid category id. Object {} remains unchanged.", fundingId,
                    mcrObjectID);
                return;
            }
        }

        // remove old funding information
        ThUniBibCommands.removeFundingInformation(mcrObject);
        // set new funding
        for (String fundingId : fundingList) {
            ThUniBibCommands.addFundingInformation(mcrObject, fundingId);
        }

        try {
            MCRMetadataManager.update(new MCRObject(mcrObject));
        } catch (Exception e) {
            LOGGER.error("Could not update {} with funding information ", mcrObjectID);
        }
    }

    @MCRCommand(syntax = "thunibib update funding of publications from url {0}",
        help = "Updates the funding of publications from the given url")
    public static List<String> updateFunding(String url) {
        LOGGER.info("Update funding of publications from url \"{}\"", url);

        List<String> commands = new ArrayList<>();

        try {
            Document document = ThUniBibCommands.getDocument(url);

            if (document == null) {
                LOGGER.error("Document is null, nothing to do");
                return commands;
            }

            XPATH_FACTORY.compile("//publication[identifier[@type = 'doi']]", Filters.element()).evaluate(document)
                .forEach(publication -> {
                    String doi = publication.getChild("identifier").getText();

                    documentsByDOI(doi).forEach(doc -> {
                        String id = doc.get("id").toString();
                        String fundings = getFundings(publication).stream()
                            .map(MCRCategory::getId)
                            .map(MCRCategoryID::getID)
                            .map(n -> String.valueOf(n))
                            .collect(Collectors.joining(ThUniBibCommands.FUNDING_SEPARATOR_CHAR));

                        commands.add("thunibib update object " + id + " with fundings " + fundings);
                    });
                });
        } catch (JDOMException | IOException e) {
            LOGGER.error("Could not create document from url {}", url, e);
        }

        return commands;
    }

    /**
     * Checks if the fundings provided are already set in the given mcr object
     * */
    private static boolean fundingChanged(Document mcrObject, List<String> newFundings) {
        XPathExpression<Element> mods = XPATH_FACTORY.compile("//mods:mods", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);

        Element modsElement = mods.evaluateFirst(mcrObject);

        if (mods == null) {
            return true;
        }

        List<String> currentFundings = new ArrayList<>();
        modsElement.getChildren("classification", MCRConstants.MODS_NAMESPACE)
            .stream()
            .filter(classElem -> classElem.getAttributeValue("authorityURI").contains("fundingType"))
            .forEach(classElem -> {
                String valueURI = classElem.getAttributeValue("valueURI");
                String value = valueURI.substring(classElem.getAttributeValue("valueURI").indexOf("#") + 1);
                currentFundings.add(value);
            });

        return !(currentFundings.containsAll(newFundings) && currentFundings.size() == newFundings.size());
    }

    /**
     * Removes any funding currently set.
     * */
    private static void removeFundingInformation(Document mcrObject) {
        XPathExpression<Element> mods = XPATH_FACTORY.compile("//mods:mods", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);

        Element modsElement = mods.evaluateFirst(mcrObject);

        if (mods == null) {
            return;
        }

        List<Element> elementList = modsElement.getChildren("classification", MODS_NAMESPACE)
            .stream()
            .filter(classElem -> classElem.getAttributeValue("authorityURI").contains("fundingType"))
            .collect(Collectors.toList());

        for (Element element : elementList) {
            element.detach();
        }
    }

    private static void addFundingInformation(Document mcrObject, String categId) {
        if (!MCRXMLFunctions.isCategoryID("fundingType", categId)) {
            LOGGER.warn("fundingType:{} is not a valid category id", categId);
            return;
        }

        XPathExpression<Element> mods = XPATH_FACTORY.compile("//mods:mods", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);

        Element modsElement = mods.evaluateFirst(mcrObject);

        if (mods == null) {
            return;
        }

        /* Add funding when funding not already set */
        if (modsElement.getChildren("classification", MCRConstants.MODS_NAMESPACE)
            .stream()
            .filter(classElem -> classElem.getAttributeValue("authorityURI").contains("fundingType"))
            .noneMatch(classElem -> classElem.getAttributeValue("valueURI").contains("fundingType#" + categId))) {

            String url = MCRFrontendUtil.getBaseURL() + "classifications/fundingType";

            Element classification = new Element("classification", MCRConstants.MODS_NAMESPACE);
            classification.setAttribute("authorityURI", url);
            classification.setAttribute("valueURI", url + "#" + categId);
            modsElement.addContent(classification);
        }
    }

    private static Document getDocument(String url) throws IOException, JDOMException {
        HttpGet get = new HttpGet(url);
        String property = "ThUniBib.Commands.Funding.PrivateToken";
        Optional<String> token = MCRConfiguration2.getString(property);

        if (token.isPresent()) {
            get.setHeader("PRIVATE-TOKEN", token.get());
        } else {
            LOGGER.warn("No token in property {} set", property);
        }

        HttpEntity entity = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse httpResponse = httpclient.execute(get);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                LOGGER.warn("Could not read from url because: {}", httpResponse.getStatusLine().getReasonPhrase());
                return null;
            }

            entity = httpResponse.getEntity();
            try (InputStream is = entity.getContent()) {
                return new SAXBuilder().build(is);
            } catch (Exception ex) {
                LOGGER.error("Could not parse xml from url {}", url);
            }
        } finally {
            if (entity != null) {
                EntityUtils.consume(entity);
            }
        }
        return null;
    }

    private static List<MCRCategory> getFundings(Element publication) {
        XPathExpression<Attribute> expr = XPATH_FACTORY.compile("fundings/funding/@type", Filters.attribute());

        List<MCRCategory> list = new ArrayList<>();
        MCRCategoryDAOImpl categoryDAO = new MCRCategoryDAOImpl();

        expr.evaluate(publication).stream()
            .map(Attribute::getValue)
            .forEach(type -> {
                list.addAll(categoryDAO.getCategoriesByLabel("de", type));
            });

        return list;
    }

    /**
     * Finds all documents with the given doi.
     *
     * @return the list of documents
     * */
    private static List<SolrDocument> documentsByDOI(String doi) {
        SolrQuery solrQuery = new SolrQuery("+id_doi:" + doi);
        solrQuery.setRows(Integer.MAX_VALUE);
        solrQuery.setSort("id", SolrQuery.ORDER.asc);

        try {
            return MCRSolrClientFactory.getMainSolrClient().query(solrQuery).getResults();
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Could not execute solr query {}", solrQuery);
            return new ArrayList<>();
        }
    }

    @MCRCommand(syntax = "thunibib create editor user {0} with email {1}",
        help = "Creates a scoped shibboleth user with login {0} and {1} as mail address")
    public static void createEditorUser(String login, String mail) {
        String unscopedUserId = Objects.requireNonNull(login, "Login must not be null");
        String eMail = Objects.requireNonNull(mail, "e-mail address must not be null");
        final Optional<MCRRealm> shibbolethRealm = MCRRealmFactory.listRealms().stream()
            .filter(r -> r.getID().endsWith(".de")).findFirst();
        if (shibbolethRealm.isEmpty()) {
            throw new MCRException("Could not find configured Shibboleth realm.");
        }
        if (MCRUserManager.exists(unscopedUserId, shibbolethRealm.get())) {
            throw new MCRException("User " + unscopedUserId + "@" + shibbolethRealm.get().getID() + " already exists.");
        }
        MCRUser shibbolethUser = new MCRUser(unscopedUserId, shibbolethRealm.get());
        DEFAULT_ROLES.forEach(shibbolethUser::assignRole);
        shibbolethUser.setLocked(true); //user may not change their details
        shibbolethUser.setDisabled(true); //no direct login allowed
        shibbolethUser.setEMail(eMail);
        MCRUserManager.createUser(shibbolethUser);
    }

    @MCRCommand(syntax = "thunibib update solr project core", help = "Updates project solr core")
    public static void updateProjects() throws IOException, JDOMException {
        HttpPost p = new HttpPost(MCRConfiguration2.getStringOrThrow("ThUniBib.FactScienceConnect.ReportURL"));
        p.setEntity(new StringEntity(MCRConfiguration2.getStringOrThrow("ThUniBib.FactScienceConnect.RequestBody")));
        p.setHeader("authorization", MCRConfiguration2.getStringOrThrow("ThUniBib.FactScienceConnect.Authorization"));
        p.setHeader("Content-Type", "application/json");

        LOGGER.info("Fetching from {}", MCRConfiguration2.getString("ThUniBib.FactScienceConnect.ReportURL").get());
        HttpEntity responseEntity = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse httpResponse = client.execute(p)) {

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                LOGGER.warn(httpResponse.getStatusLine());
                return;
            }

            responseEntity = httpResponse.getEntity();
            try (InputStream is = new BufferedInputStream(responseEntity.getContent())) {
                LOGGER.info("Parsing response...");
                Document document = new SAXBuilder().build(is);
                LOGGER.info("Parsing response...done");
                LOGGER.info("Updating solr core");
                updateSolrProjectCore(document);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not update solr's project core", ex);
        } finally {
            if (responseEntity != null) {
                EntityUtils.consume(responseEntity);
            }
        }
    }

    private static void updateSolrProjectCore(Document projects) {
        String core = MCRConfiguration2.getString("MCR.Solr.Core.ubo-projects.Name").orElse("?");

        // check for core
        if (!MCRSolrClientFactory.get(core).isPresent()) {
            LOGGER.warn("Solr core '{}' is unconfigured", core);
            return;
        }

        HttpSolrClient client = MCRSolrClientFactory.get(core).get().getClient();
        Namespace cerif = Namespace.getNamespace("cerif", "https://www.openaire.eu/cerif-profile/1.1/");

        XPathExpression<Element> titleExpr = XPathFactory.instance()
            .compile("./cerif:Title[@xml:lang='de']", Filters.element(), null, cerif);

        XPathExpression<Element> projectIdExpr = XPathFactory.instance()
            .compile("./cerif:Identifier[@type='https://w3id.org/cerif/vocab/IdentifierTypes#Project-MM-ID']",
                Filters.element(), null, cerif);

        XPathExpression<Element> funderExpr = XPathFactory.instance()
            .compile("./cerif:Identifier[@type='https://w3id.org/cerif/vocab/IdentifierTypes#Finance-ID']",
                Filters.element(), null, cerif);

        XPathExpression<Element> funderNoExpr = XPathFactory.instance()
            .compile("./cerif:Funded/cerif:As/cerif:Funding", Filters.element(), null, cerif);

        Attribute attribute = XPathFactory.instance().compile("./response/@numFound", Filters.attribute(), null)
            .evaluateFirst(projects);

        AtomicInteger counter = new AtomicInteger(0);
        XPathFactory.instance().compile("//cerif:Project", Filters.element(), null, cerif).evaluate(projects)
            .parallelStream().forEach(p -> {
                String projectTitle = titleExpr.evaluateFirst(p).getText();
                String projectId = projectIdExpr.evaluateFirst(p).getText();
                String acronym = p.getChildText("Acronym", cerif);
                String funder = funderExpr.evaluateFirst(p).getText();
                String fundingNumber = funderNoExpr.evaluateFirst(p).getText();

                SolrInputDocument sd = new SolrInputDocument();
                sd.addField("project_id", projectId);
                sd.addField("acronym", acronym);
                sd.addField("project_title", projectTitle);
                sd.addField("funder", funder);
                sd.addField("funding_number", fundingNumber);
                sd.addField("project_search_all",
                    projectId + " " + projectTitle + " " + acronym + " " + funder + " " + fundingNumber);
                try {
                    client.add(sd);
                    counter.getAndIncrement();
                } catch (SolrServerException | IOException e) {
                    throw new RuntimeException(e);
                }
            });

        LOGGER.info("Expected {} projects and a total of {} projects were indexed",
            attribute != null ? attribute.getValue() : "?", counter.get());
        LOGGER.info("Optimizing project index");
        MCRSolrCommands.optimize(core);
    }

    @MCRCommand(
        syntax = "list vanished ldap users for realm {0}",
        help = "List ldap users linked to publications but are not findable via ldap search"
    )
    public static List<MCRUser> listVanishedLDAPUsers(String realm) {
        boolean noneMatch = MCRRealmFactory.listRealms().stream().noneMatch(r -> r.getID().equals(realm));

        if (noneMatch) {
            LOGGER.error("Realm '{}' is unknown", realm);
            return null;
        }

        List<MCRUser> users = MCRUserManager.listUsers(null, realm, null);
        if (users.size() == 0) {
            LOGGER.info("No users found for realm '{}'", realm);
            return users;
        }

        List<MCRUser> missing = new ArrayList<>();

        try {
            LdapContext ctx = new LDAPAuthenticator().authenticate();
            LDAPSearcher searcher = new LDAPSearcher();
            users.stream()
                .filter(user -> getLeadIdAttribute(user).isPresent())
                .forEach(user -> {
                    String leadId = getLeadIdAttribute(user).get().getValue();
                    if (!exists(searcher, ctx, leadId)) {
                        missing.add(user);
                    }
                });
        } catch (NamingException e) {
            LOGGER.error("", e);
        }

        LOGGER.info("{}/{} user(s) of realm '{}' are orphaned", missing.size(), users.size(), realm);
        if (missing.size() > 0) {
            LOGGER.debug("The following users could not be found in ldap");
            for (MCRUser u : missing) {
                LOGGER.debug(u.getUserID());
            }
        }
        return missing;
    }

    @MCRCommand(syntax = "move user {0} to realm {1}", help = "Moves the given user to the given realm")
    public static boolean moveUserToRealm(String userName, String toRealmId) {
        if (userName == null || toRealmId == null) {
            LOGGER.error("Neither username or realmId may be null");
            return false;
        }

        MCRUser mcrUser = MCRUserManager.getUser(userName);
        if (mcrUser == null) {
            LOGGER.error("User {} could not be found", userName);
            return false;
        }

        MCRRealm toRealm = MCRRealmFactory.getRealm(toRealmId);
        if (toRealm == null) {
            LOGGER.error("{} is unknown", toRealmId);
            return false;
        }

        MCRRealm fromRealm = MCRRealmFactory.getRealm(userName.split("@")[1]);
        if (fromRealm == null) {
            LOGGER.error("{} is unknown", fromRealm);
            return false;
        }

        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = em.createQuery("UPDATE MCRUser SET realmID = :realmId WHERE userName = :userName");
        query.setParameter("realmId", toRealmId);
        query.setParameter("userName", mcrUser.getUserName());

        LOGGER.info("Moving user '{}' from realm '{}' to realm '{}'", userName, fromRealm.getID(), toRealm.getID());
        int updateCount = query.executeUpdate();

        return updateCount > 0;
    }

    private static Optional<MCRUserAttribute> getLeadIdAttribute(MCRUser user) {
        Optional<MCRUserAttribute> attribute = user.getAttributes()
            .stream()
            .filter(a -> a.getName().equals(LDAP_ID_NAME))
            .findFirst();
        return attribute;
    }

    private static boolean exists(LDAPSearcher searcher, LdapContext ctx, String ldapIdAttr) {
        try {
            String filter = "(&(objectClass=eduPerson)(|(eduPersonUniqueId=" + ldapIdAttr + ")))";
            List<LDAPObject> ldapObjects = searcher.searchWithGlobalDN(ctx, filter);
            return ldapObjects.size() == 1 ? true : false;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
