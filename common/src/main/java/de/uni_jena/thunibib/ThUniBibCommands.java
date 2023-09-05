package de.uni_jena.thunibib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
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

import static org.mycore.common.MCRConstants.XPATH_FACTORY;

@MCRCommandGroup(name = "ThUniBib Tools")

public class ThUniBibCommands {
    private static final Logger LOGGER = LogManager.getLogger(ThUniBibCommands.class);

    private static List<String> DEFAULT_ROLES = List.of("submitter", "admin");

    private static final String LDAP_ID_NAME = "id_" + MCRConfiguration2.getString("UBO.projectid.default").get();

    @MCRCommand(syntax = "thunibib update funding of publications from url {0}",
        help = "Updates the funding of publications from the given url")
    public static void updateFunding(String url) throws MalformedURLException {
        LOGGER.info("Update funding of publications from url \"{}\"", url);
        try {
            SAXBuilder b = new SAXBuilder();
            Document document = b.build(new URL(url));

            XPATH_FACTORY.compile("//publication[identifier[@type = 'doi']]", Filters.element()).evaluate(document)
                .forEach(publication -> {
                    String doi = publication.getChild("identifier").getText();

                    documentsByDOI(doi).forEach(doc -> {
                        MCRObjectID id = MCRObjectID.getInstance(doc.get("id").toString());
                        Document mcrObject = MCRMetadataManager.retrieveMCRObject(id).createXML();

                        getFundings(publication)
                            .stream()
                            .map(MCRCategory::getId)
                            .map(MCRCategoryID::getID)
                            .forEach(funding -> {
                                LOGGER.info("{}: adding \"{}\" as funding information", id, funding);
                                addFundingInformation(mcrObject, funding);
                            });

                        try {
                            MCRMetadataManager.update(new MCRObject(mcrObject));
                        } catch (MCRAccessException e) {
                            LOGGER.error("Could not update {} with funding information ", id);
                        }
                    });
                });
        } catch (JDOMException | IOException e) {
            LOGGER.error("Could not create document from url {}", url, e);
        }
    }

    private static void addFundingInformation(Document mcrObject, String categId) {
        String url = MCRFrontendUtil.getBaseURL() + "classifications/fundingType";
        XPathExpression<Element> mods = XPATH_FACTORY.compile("//mods:mods", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);

        Element classification = new Element("classification", MCRConstants.MODS_NAMESPACE);
        classification.setAttribute("authorityURI", url);
        classification.setAttribute("valueURI", url + "#" + categId);
        mods.evaluateFirst(mcrObject).addContent(classification);
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

    private static List<SolrDocument> documentsByDOI(String doi) {
        SolrQuery solrQuery = new SolrQuery("-fundingType:[* TO *] +id_doi:" + doi);
        solrQuery.setRows(Integer.MAX_VALUE);
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

        HttpEntity responseEntity = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            LOGGER.info("Fetching from {}", MCRConfiguration2.getString("ThUniBib.FactScienceConnect.ReportURL").get());
            CloseableHttpResponse httpResponse = client.execute(p);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                LOGGER.warn(httpResponse.getStatusLine());
                return;
            }

            responseEntity = httpResponse.getEntity();
            String source = EntityUtils.toString(responseEntity, "UTF-8");

            SAXBuilder b = new SAXBuilder();
            try (InputStream is = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))) {
                LOGGER.info("Parsing response...");
                Document document = b.build(is);
                LOGGER.info("Parsing response...done");
                LOGGER.info("Updating solr core");
                updateSolrProjectCore(document);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not update solr's project core", ex);
        } finally {
            EntityUtils.consume(responseEntity);
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
            LOGGER.info("The following users could not be found in ldap");
            for (MCRUser u : missing) {
                LOGGER.info(u.getUserID());
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
