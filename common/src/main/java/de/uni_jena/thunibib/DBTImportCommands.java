package de.uni_jena.thunibib;

import de.uni_jena.thunibib.impex.DBTImportIdProvider;
import de.uni_jena.thunibib.impex.importer.ConfigurableListImportJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrCore;
import org.mycore.ubo.importer.ListImportJob;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@MCRCommandGroup(name = "ThUniBib DBT-Import")
public class DBTImportCommands {
    private static final Logger LOGGER = LogManager.getLogger(DBTImportCommands.class);

    protected static final String DBT_BASEURL = MCRConfiguration2.getStringOrThrow("ThUniBib.Importer.DBT.BaseURL");

    protected static final HttpSolrClient DBT_SOLR_CLIENT;

    protected static final MCRSolrCore DBT_SOLR_CORE = MCRSolrClientFactory.get("dbt").orElseThrow();

    static {
        DBT_SOLR_CLIENT = DBT_SOLR_CORE.getClient();
    }

    @MCRCommand(syntax = "query dbt solr {0}", help = "Returns the number of hits for the given solr query (DBT-core)")
    public static void checkDBTSolrReachable(String query) throws Exception {
        QueryResponse response = DBT_SOLR_CLIENT.query(new SolrQuery(query));
        LOGGER.info("{} hit(s) for query '{}' to DBT's solr core at '{}'", response.getResults().getNumFound(), query,
            DBT_SOLR_CORE.getServerURL());
    }

    @MCRCommand(syntax = "import from dbt by solr query {0}",
        help = "Import new objects from DBT by the given solr query")
    public static List<String> importFromDBTByQuery(String solrQuery) {
        LOGGER.info("Importing from {} with solr query '{}'", DBT_BASEURL, solrQuery);

        SolrDocumentList solrDocuments = null;
        try {
            SolrQuery query = new SolrQuery(solrQuery);
            query.setRows(Integer.MAX_VALUE);
            query.setFields("id", "mods.identifier");
            query.setFacet(false);

            solrDocuments = DBT_SOLR_CLIENT.query(query).getResults();
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Could not get solr documents for query '{}'", solrQuery);
            return null;
        }

        List<String> identifiers = getIdentifiers(solrDocuments);
        if (identifiers.isEmpty()) {
            LOGGER.info("No new DBT MyCoRe object identifiers could be found");
            return null;
        }

        String eligible = identifiers.stream().collect(Collectors.joining(" "));
        LOGGER.info("The following {} DBT identifiers will be considered for import: {}", identifiers.size(), eligible);
        return Arrays.asList("import " + eligible + " from dbt");
    }

    /**
     * Extracts the identifiers from Solr documents that have not been published in ThUniBib yet.
     *
     * @param solrDocumentList A {@link SolrDocumentList} containing the Solr documents to be processed.
     * @return A {@code List} of String objects representing the identifiers of the documents
     *         that have not been published. The list will be empty if all documents are already
     *         published or if the input list contains no documents.
     */
    private static List<String> getIdentifiers(SolrDocumentList solrDocumentList) {
        return solrDocumentList.stream()
            .filter(doc -> !publicationExists(doc))
            .map(doc -> doc.get("id"))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    /**
     * Checks if a publication already exists in the database.
     *
     * This method queries the Solr server to determine whether a given SolrDocument
     * contains any identifiers ('mods.identifier') that match an existing publication's
     * identifier in the Solr index. It performs a Solr query for each identifier found
     * in the 'mods.identifier' field of the document and checks if there are any matching
     * records in the Solr index.
     *
     * @param doc The SolrDocument to check for existing identifiers.
     * @return true if at least one identifier from the document matches an existing publication's identifier
     *         in the Solr index, otherwise false.
     *
     * <p>Note: In case of any SolrServerException or IOException during the query execution,
     * the method logs the error and treats the identifier as existing to prevent potential
     * duplicates. This conservative approach ensures data integrity at the expense of possibly
     * ignoring new publications that failed to be queried successfully.</p>
     */
    private static boolean publicationExists(SolrDocument doc) {
        Collection<Object> fieldValues = doc.getFieldValues("mods.identifier");
        String dbtId = doc.get("id").toString();

        if (fieldValues == null) {
            LOGGER.warn("No field 'mods.identifier' present in document of '{}'", doc.get("id"));
            fieldValues = Collections.emptyList();
        }

        fieldValues.add(dbtId);

        return fieldValues
            .stream()
            .map(Object::toString)
            .anyMatch(identifier -> {
                try {
                    return MCRSolrClientFactory
                        .getMainSolrClient()
                        .query(new SolrQuery("+pub_id:" + identifier))
                        .getResults()
                        .getNumFound() > 0;
                } catch (SolrServerException | IOException e) {
                    LOGGER.error("Could not query local solr for identifier {}", identifier, e);
                    return true;
                }
            });
    }

    @MCRCommand(syntax = "import {0} from dbt",
        help = "Imports the objects from the DBT using their identifiers. Multiple identifiers can be specified separated by blank characters.")
    public static void importFromDBTByIdentifierList(String dbtid) {
        LOGGER.info("Importing DBT object {} from dbt", dbtid);

        Element config = new Element("import");
        config.setAttribute("targetType", "import");
        config.addContent(new Element("source").setText(dbtid));
        ListImportJob job = new ConfigurableListImportJob("DBTList", new DBTImportIdProvider());

        try {
            job.transform(config);
            List<MCRObject> imported = job.savePublications();
            ThUniBibMailer.sendMail(job.getID(), imported, "imported", "DBT");
        } catch (Exception e) {
            LOGGER.error("Could not save imported DBT object {}", dbtid, e);
        }
    }
}
