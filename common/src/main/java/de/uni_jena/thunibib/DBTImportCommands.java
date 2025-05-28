package de.uni_jena.thunibib;

import de.uni_jena.thunibib.impex.DBTImportIdProvider;
import de.uni_jena.thunibib.impex.importer.ConfigurableListImportJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRMailer;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.ubo.importer.ImportJob;
import org.mycore.ubo.importer.ListImportJob;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@MCRCommandGroup(name = "ThUniBib DBT-Import")
public class DBTImportCommands {
    private static final Logger LOGGER = LogManager.getLogger(DBTImportCommands.class);

    @MCRCommand(syntax = "thunibib import from dbt by solr query {0}",
        help = "Import new objects from DBT by the given solr query")
    public static void importFromDBTByQuery(String solrQuery) {
        LOGGER.info("Importing from DBT by the given solr query");


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
