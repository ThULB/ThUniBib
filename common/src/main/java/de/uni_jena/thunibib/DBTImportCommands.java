package de.uni_jena.thunibib;

import de.uni_jena.thunibib.impex.DBTImportIdProvider;
import de.uni_jena.thunibib.impex.importer.ConfigurableListImportJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.mycore.common.MCRMailer;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.ubo.importer.ImportJob;
import org.mycore.ubo.importer.ListImportJob;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mycore.common.MCRConstants.XPATH_FACTORY;

@MCRCommandGroup(name = "ThUniBib DBT-Import")
public class DBTImportCommands {
    private static final Logger LOGGER = LogManager.getLogger(DBTImportCommands.class);

    protected static final String DBT_BASEURL = MCRConfiguration2.getStringOrThrow("ThUniBib.Importer.DBT.BaseURL");

    @MCRCommand(syntax = "thunibib import from dbt by solr query {0}",
        help = "Import new objects from DBT by the given solr query")
    public static List<String> importFromDBTByQuery(String solrQuery) {
        LOGGER.info("Importing from {} with solr query '{}'", DBT_BASEURL, solrQuery);
        URL url;

        try {
            String queryString = toQueryString(solrQuery);
            url = new URL(DBT_BASEURL + "servlets/solr/select?" + queryString);
        } catch (MalformedURLException e) {
            LOGGER.error("Could not build url from {} and {}", DBT_BASEURL, solrQuery, e);
            return null;
        }

        Document solrDocument = getSolrDocument(url);
        if (solrDocument == null) {
            LOGGER.warn("Could not get solr document from {}", url);
            return null;
        }

        List<String> identifiers = getIdentifiers(solrDocument);
        if (identifiers.isEmpty()) {
            LOGGER.info("No new identifiers could be found");
            return null;
        }

        String eligable = identifiers.stream().collect(Collectors.joining(" "));
        LOGGER.info("The following {} DBT identifiers will be considered for import: {}", identifiers.size(), eligable);
        return Arrays.asList("thunibib import " + eligable + " from dbt");
    }

    private static List<String> getIdentifiers(Document solrDocument) {
        XPathExpression<Text> text = XPATH_FACTORY.compile("str[@name='id']/text()", Filters.text());

        return XPATH_FACTORY.compile("//doc[str[@name='id']]", Filters.element())
            .evaluate(solrDocument)
            .stream()
            .filter(doc -> !publicationExists(doc))
            .map(doc -> text.evaluateFirst(doc))
            .filter(Objects::nonNull)
            .map(Text::getText)
            .collect(Collectors.toList());
    }

    private static boolean publicationExists(Element doc) {
        return XPATH_FACTORY.compile("//arr[@name='mods.identifier']/str/text()", Filters.text())
            .evaluate(doc)
            .stream()
            .map(Text::getText)
            .anyMatch(identifier -> {
                try {
                    return MCRSolrClientFactory
                        .getMainSolrClient()
                        .query(new SolrQuery("+pub_id:" + identifier))
                        .getResults().getNumFound() > 0;
                } catch (SolrServerException | IOException e) {
                    LOGGER.error("Could not query local solr for identifier {}", identifier, e);
                    return true;
                }
            });
    }

    private static String toQueryString(String solrQuery) {
        StringBuilder b = new StringBuilder();

        String[] nameValues = solrQuery.split("&");
        for (int i = 0; i < nameValues.length; i++) {
            String[] split = nameValues[i].split("=");
            if (split.length == 2) {
                b.append(
                    (b.length() > 0 ? "&" : "") + split[0] + "=" + URLEncoder.encode(split[1], StandardCharsets.UTF_8));
            }
        }

        return b.toString();
    }

    private static Document getSolrDocument(URL url) {
        try (BufferedInputStream is = new BufferedInputStream(url.openStream())) {
            SAXBuilder builder = new SAXBuilder();
            return builder.build(is);
        } catch (IOException e) {
            LOGGER.error("Could not open stream for url {}", url, e);
        } catch (JDOMException e) {
            LOGGER.error("Could not build xml", e);
        }
        return null;
    }

    @MCRCommand(syntax = "thunibib import {0} from dbt",
        help = "Imports the objects from the DBT using their identifiers. Multiple identifiers can be specified separated by blank characters.")
    public static void importFromDBTByIdentifierList(String dbtid) {
        LOGGER.info("Importing DBT object {} from dbt", dbtid);

        Element config = new Element("import");
        config.setAttribute("targetType", "import");
        config.addContent(new Element("source").setText(dbtid));
        ListImportJob job = new ConfigurableListImportJob("DBTList", new DBTImportIdProvider());

        try {
            job.transform(config);
            job.savePublications();
        } catch (Exception e) {
            LOGGER.error("Could not save imported DBT object {}", dbtid, e);
        }
        sendMail(job);
    }

    private static void sendMail(ImportJob job) {
        String subject = "DBT-Import " + job.getID();
        String from = MCRConfiguration2.getString("UBO.Mail.From").get();
        String to = MCRConfiguration2.getString("MCR.Mail.Address").get();
        String url = MCRFrontendUtil.getBaseURL() + "servlets/solr/select?fq=%2BobjectType%3Amods&q=importID%3A%22"
            + URLEncoder.encode(job.getID(), StandardCharsets.UTF_8) + "%22";
        String body = MCRTranslation.translateToLocale("ubo.import.list.email.body", url, "de");

        MCRMailer.send(from, to, subject, body);
    }
}
