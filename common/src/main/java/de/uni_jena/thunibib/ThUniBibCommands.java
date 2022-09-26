package de.uni_jena.thunibib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrCore;
import org.mycore.solr.commands.MCRSolrCommands;
import org.mycore.user2.MCRRealm;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

@MCRCommandGroup(name = "ThUniBib Tools")

public class ThUniBibCommands {
    private static final Logger LOGGER = LogManager.getLogger(ThUniBibCommands.class);

    private static List<String> DEFAULT_ROLES = List.of("submitter", "admin");

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
}
