package de.uni_jena.thunibib.enrichment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.enrichment.MCREnrichmentResolver;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@MCRCommandGroup(name = "experimental DBT import commands (by affiliation gnd)")
public class EnrichmentByAffiliationCommands extends MCRAbstractCommands {

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods",
            "http://www.loc.gov/mods/v3");
    public static final Namespace NAMESPACE_ZS = Namespace.getNamespace("zs",
            "http://www.loc.gov/zing/srw/");

    private static final Logger LOGGER = LogManager.getLogger(EnrichmentByAffiliationCommands.class);

    private static final String projectID = "ubo";

    private static final String PPN_IDENTIFIER = "ppn";

    private static List<String> DUPLICATE_CHECK_IDS;
    private static String K10PLUS_MAX_RECORDS;
    private static String LOBID_MAX_RECORDS;

    static {
        MCRConfiguration config = MCRConfiguration.instance();

        String property_prefix = "UBO.ThuniBib.jena.affilitation.import.";
        String property_duplicates_check = property_prefix + "dublicate.check.identifiers";
        String property_k10plus_max_records = property_prefix + "k10plus.max.records";
        String property_lobid_max_records = property_prefix + "lobid.max.records";

        String ids = config.getString(property_duplicates_check, "issn,isbn,doi");
        DUPLICATE_CHECK_IDS = Arrays.asList(ids.split(","));
        LOGGER.info("Checking for duplicates using these identifier: {}", DUPLICATE_CHECK_IDS);

        K10PLUS_MAX_RECORDS = config.getString(property_k10plus_max_records, "1000");
        LOGGER.info("Max records that are queried from k10plus: {}", K10PLUS_MAX_RECORDS);

        LOBID_MAX_RECORDS = config.getString(property_lobid_max_records, "10000");
        LOGGER.info("Max records that are queried from lobid: {}", LOBID_MAX_RECORDS);
    }


    private static final String QUERY_PLACEHOLDER = "${query}";
    private static final int BATCH_SIZE = 10;
    private static final String PICA_URL = "https://sru.k10plus.de/opac-de-ilm1?version=1.1&operation=searchRetrieve&query="
        + QUERY_PLACEHOLDER + "m&maximumRecords="+BATCH_SIZE+"&recordSchema=mods36";
    private static final String PICA_IMPORT_SYNTAX = "test import by pica query with {0} and start {1} and status {2}";
    private static final String ENRICH_PPN_SYNTAX = "test enrich ppn {0} with status {1}";

    /* pica.lsw%3Dil */
    @MCRCommand(syntax = PICA_IMPORT_SYNTAX, help = "imports all objects for a specific query",order = 5)
    public static List<String> testImportByPicaQuery(String picaQuery, String startStr, String status) {
        final String request = buildRequestURL(picaQuery, startStr);
        final Element result = Objects.requireNonNull(MCRURIResolver.instance().resolve(request));

        XPathExpression<Element> r = XPathFactory.instance().compile(".//mods:mods/mods:recordInfo/mods:recordIdentifier",
                Filters.element(), null, NAMESPACE_ZS, MODS_NAMESPACE);
        List<Element> recordIdentifierElements = r.evaluate(result);

        List<String> commands = new ArrayList<>(recordIdentifierElements.size());

        recordIdentifierElements.stream().map(Element::getTextNormalize)
                .map((ppn) -> ENRICH_PPN_SYNTAX.replace("{0}",ppn).replace("{1}", status))
                .forEach(commands::add);

        final String numberOfRecordsStr = result.getChildTextNormalize("numberOfRecords", NAMESPACE_ZS);
        final int numberOfRecords = Integer.parseInt(numberOfRecordsStr);
        final int start = Integer.parseInt(startStr);

        if((start+BATCH_SIZE)<numberOfRecords){
            commands.add(PICA_IMPORT_SYNTAX.replace("{0}",picaQuery)
                    .replace("{1}",""+(start+BATCH_SIZE))
                    .replace("{2}",status));
        }

        return commands;
    }

    @MCRCommand(syntax = ENRICH_PPN_SYNTAX, help = "Imports document with ppn and enrichment resolver")
    public static void testEnrichPPN(String ppnID,String status){
        MCRMODSWrapper wrappedMods = createMinimalMods(ppnID);
        new MCREnrichmentResolver().enrichPublication(wrappedMods.getMODS(), "import");
        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(wrappedMods.getMCRObject().createXML()));
        createOrUpdate(wrappedMods, status);
    }

    public static String buildRequestURL(String query, String start) {
        return PICA_URL.replace(QUERY_PLACEHOLDER, query) + "&startRecord=" + start;
    }

    private final static SimpleDateFormat ID_BUILDER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private static String getImportID() {
        return ID_BUILDER.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime()));
    }

    @MCRCommand(syntax = "test import by affiliation with GND {0} and status {1}",
            help = "test import of publications by affiliation GND, imported documents get status one of 'confirmed', " +
                    "'submitted', 'imported'",
            order = 10
    )
    public static void testImportByAffiliation(String affiliationGND, String import_status) {
        final List<String> allowedStatus = Arrays.asList(new String[]{"confirmed", "submitted", "imported"});

        // HashSets for finding duplicates during the import session, PPN is hard wired, rest is configurable
        Map<String, Set<String>> duplicateCheckSetsMap = setUpDuplicateCheckSets();

        if(allowedStatus.contains(import_status)) {

            List<String> affiliatedPersonsGNDs = getAllGNDsOfAffiliatedPersons(affiliationGND);
            for(String affiliatedPersonGND : affiliatedPersonsGNDs) {
                Map<String, Document> ppnToPublicationMap = getAllPublicationsOfPersonByGND(affiliatedPersonGND);
                for (String ppnID : ppnToPublicationMap.keySet()) {
                    if(!checkForDuplicates(ppnID, ppnToPublicationMap.get(ppnID), duplicateCheckSetsMap)) {
                        MCRMODSWrapper wrappedMods = createMinimalMods(ppnID);
                        new MCREnrichmentResolver().enrichPublication(wrappedMods.getMODS(), "import");
                        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(wrappedMods.getMCRObject().createXML()));
                        createOrUpdate(wrappedMods, import_status);
                    } else {
                        LOGGER.info("Publication with PPN {} was already imported, import canceled!", ppnID);
                    }
                }
            }

        } else {
            LOGGER.info("Status not allowed: {}, use one of {}", import_status, String.join(", ", allowedStatus));
        }

    }

    private static Map<String, Set<String>> setUpDuplicateCheckSets() {
        Map<String, Set<String>> duplicateCheckSetsMap = new HashMap<>();
        duplicateCheckSetsMap.put(PPN_IDENTIFIER, new HashSet<>());
        for(String checkID : DUPLICATE_CHECK_IDS) {
            duplicateCheckSetsMap.put(checkID, new HashSet<>());
        }
        return duplicateCheckSetsMap;
    }

    /**
     * Given a publication and its PPN, check for duplicates in current import session via HashSets and across sessions
     * via Solr queries. This method has side effects. The sets in the duplicateCheckSetsMap get extended by the
     * identifiers of the publication that are used for finding duplicates by each method call. Meaning that the first
     * call with a new unique publication would return "false", meaning there where no duplicates found, while another
     * call with the same parameters (same publication and ppn) would return "true", meaning this publication was
     * already imported, either this import session or sometime before in a different session.
     *
     * @param publication_ppn The PPN of the publication
     * @param publication The publication in question
     * @param duplicateCheckSetsMap A map containing HashSets of different identifiers and their corresponding values
     *                              to check for duplicates during an import session
     * @return false, if no duplicate was found or true if a duplicate was found
     */
    private static boolean checkForDuplicates(String publication_ppn, Document publication,
                                              Map<String, Set<String>> duplicateCheckSetsMap) {

        // Check for duplicates using PPN (hard wired)
        // 1. check HashSet (duplicates per import session)
        if(duplicateCheckSetsMap.get(PPN_IDENTIFIER).contains(publication_ppn)) {
            LOGGER.info("Found duplicate in import session by PPN {}", publication_ppn);
            return true;
        } else {
            duplicateCheckSetsMap.get(PPN_IDENTIFIER).add(publication_ppn);
        }
        // 2. check against Solr (duplicates across multiple imports)
        String ppn_field = "id_" + PPN_IDENTIFIER;
        if(isAlreadyStored(ppn_field , publication_ppn)) {
            LOGGER.info("Found duplicate in Solr by field {} and value {}", ppn_field, publication_ppn);
            return true;
        }

        // Check for duplicates using configured IDs
        // 1. Check HashSets (duplicates per import session)
        for(String checkID : DUPLICATE_CHECK_IDS) {
            List<Element> identifierElements = getIdentifierElementsByType(publication, checkID);
            for (Element identifierElement : identifierElements) {
                String identifierValue = identifierElement.getValue();
                if (duplicateCheckSetsMap.get(checkID).contains(identifierValue)) {
                    LOGGER.info("Found duplicate in import session by {} {}", checkID, identifierValue);
                    return true;
                } else {
                    duplicateCheckSetsMap.get(checkID).add(identifierValue);
                }
                // 2. check against Solr (duplicates across multiple imports)
                String solr_field = "id_" + checkID;
                if (isAlreadyStored(solr_field, identifierValue)) {
                    LOGGER.info("Found duplicate in Solr by field {} and value {}", solr_field, publication_ppn);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAlreadyStored(String identifier_field, String identifier_value) {
        LOGGER.info("Check if already stored in Solr using field {} and value {}", identifier_field, identifier_value);
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(identifier_field + ":" + MCRSolrUtils.escapeSearchValue(identifier_value));
        query.setRows(0);
        SolrDocumentList results;
        try {
            results = solrClient.query(query).getResults();
            return (results.getNumFound() > 0);
        } catch (Exception ex) {
            throw new MCRException(ex);
        }
    }

    private static List<Element> getIdentifierElementsByType(Document publication, String type) {
        List<Element> identifierElements = new ArrayList<>();

        XPathFactory xFactory = XPathFactory.instance();
        XPathExpression<Element> identifierElementExpr = xFactory.compile("//mods:identifier[@type='" + type + "']",
                Filters.element(), null, NAMESPACE_ZS, MODS_NAMESPACE);
        identifierElements = identifierElementExpr.evaluate(publication);

        return identifierElements;
    }

    private static Map<String, Document> getAllPublicationsOfPersonByGND(String gnd) {
        Map<String, Document> ppnToPublicationMap = new HashMap<>();

        String targetURL = "http://sru.k10plus.de/gvk?version=1.1&operation=searchRetrieve&query=pica.nid%3D" +
                gnd + "&maximumRecords=" + K10PLUS_MAX_RECORDS + "&recordSchema=mods36";
        String requestContent = makeRequest(targetURL);

        SAXBuilder builder = new SAXBuilder();
        try {
            Document searchResponseXML = builder.build(new InputSource(new StringReader(requestContent)));

            XPathFactory xFactory = XPathFactory.instance();

            // get all publications of person
            XPathExpression<Element> modsRecordExpr = xFactory.compile("//mods:mods",
                    Filters.element(), null, NAMESPACE_ZS, MODS_NAMESPACE);
            List<Element> publicationElements = modsRecordExpr.evaluate(searchResponseXML);
            LOGGER.info("Got {} publications for Person by GND {}", publicationElements.size(), gnd);

            for(Element publication : publicationElements) {
                // get the PPN of each publication and map it
                XPathExpression<Element> recordIdentifierExpr = xFactory.compile("//mods:recordIdentifier",
                        Filters.element(), null, NAMESPACE_ZS, MODS_NAMESPACE);
                Element recordIdentifierElement = recordIdentifierExpr.evaluateFirst(publication);
                if(recordIdentifierElement != null) {
                    String ppn = recordIdentifierElement.getValue();
                    publication.detach();
                    ppnToPublicationMap.put(ppn, new Document(publication));
                } else {
                    LOGGER.error("Publication without PPN found for GND {}, skipping", gnd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ppnToPublicationMap;
    }

    private static MCRMODSWrapper createMinimalMods(String ppnID) {
        LOGGER.debug("Start createMinimalMods");

        final Element modsRoot = new Element("mods", MCRConstants.MODS_NAMESPACE);
        final Element identifierElement = new Element("identifier", MCRConstants.MODS_NAMESPACE);
        identifierElement.setAttribute("type", "ppn");
        identifierElement.setText(ppnID);

        final MCRObject ppn;
        ppn = MCRMODSWrapper.wrapMODSDocument(modsRoot, projectID);
        ppn.setId(nextFreeID());

        final MCRMODSWrapper wrapper = new MCRMODSWrapper(ppn);
        wrapper.addElement(identifierElement);

        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(wrapper.getMCRObject().createXML()));
        LOGGER.debug("End createMinimalMods");
        return wrapper;
    }

    private static String makeRequest(String targetURL) {
        String content = "";
        try {
            URL url = new URL(targetURL);
            URLConnection request = url.openConnection();
            request.connect();
            content = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return content;
    }

    private static List<String> getAllGNDsOfAffiliatedPersons(String affiliationGND) {
        List<String> affiliatedPersonsGNDs = new ArrayList<>();

        String targetURL = "http://lobid.org/gnd/search?q=" + affiliationGND +
                "&filter=type%3APerson&format=json&size=" + LOBID_MAX_RECORDS;
        String requestContent = makeRequest(targetURL);

        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(requestContent, JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        LOGGER.info("Received information of {} affiliated persons for affiliation GND: {}",
                jsonObject.get("totalItems"), affiliationGND);
        for(JsonElement element : jsonObject.getAsJsonArray("member")) {
            JsonObject member = element.getAsJsonObject();
            String gndIdentifier = member.get("gndIdentifier").getAsString();
            affiliatedPersonsGNDs.add(gndIdentifier);
        }
        LOGGER.debug("Got the following GNDs of affiliated persons with affiliation GND {}: ", affiliatedPersonsGNDs);
        return affiliatedPersonsGNDs;
    }

    private static MCRObjectID nextFreeID() {
        return MCRObjectID.getNextFreeId(projectID, "mods");
    }

    private static void createOrUpdate(MCRMODSWrapper wrappedMCRobj, String import_status) {
        wrappedMCRobj.setServiceFlag("importID","SRU-PPN-" + getImportID());
        MCRObject object = wrappedMCRobj.getMCRObject();
        // save object
        try {
            setState(wrappedMCRobj, import_status);
            if(MCRMetadataManager.exists(object.getId())) {
                LOGGER.info("Update object {}!", object.getId().toString());
                MCRMetadataManager.update(object);
            } else {
                LOGGER.info("Create object {}!", object.getId().toString());
                MCRMetadataManager.create(object);
            }
        } catch (MCRAccessException e) {
            throw new MCRException("Error while creating " + object.getId().toString(), e);
        }
    }

    private static void setState(MCRMODSWrapper wrapped, String import_status) {
        wrapped.setServiceFlag("status", import_status);
    }
}
