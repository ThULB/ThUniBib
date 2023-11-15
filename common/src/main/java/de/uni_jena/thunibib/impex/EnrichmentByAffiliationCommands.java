package de.uni_jena.thunibib.impex;

import java.io.IOException;
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
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.uni_jena.thunibib.ThUniBibMailer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.enrichment.MCREnrichmentResolver;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueue;
import org.mycore.services.queuedjob.MCRJobQueueManager;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@MCRCommandGroup(name = "Catalog Import Commands")
public class EnrichmentByAffiliationCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(EnrichmentByAffiliationCommands.class);
    private static final CommandTracker<String, MCRObject> TRACKER = new CommandTracker<>();

    private static final String projectID = MCRConfiguration2.getStringOrThrow("UBO.projectid.default");

    private final static SimpleDateFormat ID_BUILDER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String PPN_IDENTIFIER = "ppn";

    private static List<String> DUPLICATE_CHECK_IDS;
    private static String K10PLUS_MAX_RECORDS;
    private static String LOBID_MAX_RECORDS;

    static {
        String property_prefix = "ThUniBib.affilitation.import.";
        String property_duplicates_check = property_prefix + "dublicate.check.identifiers";
        String property_k10plus_max_records = property_prefix + "k10plus.max.records";
        String property_lobid_max_records = property_prefix + "lobid.max.records";

        DUPLICATE_CHECK_IDS = MCRConfiguration2
            .getOrThrow(property_duplicates_check, MCRConfiguration2::splitValue)
            .collect(Collectors.toList());
        LOGGER.info("Checking for duplicates using these identifier: {}", DUPLICATE_CHECK_IDS);

        K10PLUS_MAX_RECORDS = MCRConfiguration2.getStringOrThrow(property_k10plus_max_records);
        LOGGER.info("Max records that are queried from k10plus: {}", K10PLUS_MAX_RECORDS);

        LOBID_MAX_RECORDS = MCRConfiguration2.getStringOrThrow(property_lobid_max_records);
        LOGGER.info("Max records that are queried from lobid: {}", LOBID_MAX_RECORDS);
    }

    private static final String SRU_DATABASE = MCRConfiguration2.getStringOrThrow("MCR.PICA2MODS.DATABASE");
    private static final String QUERY_PLACEHOLDER = "${query}";
    private static final int BATCH_SIZE = 10;
    private static final String PICA_URL = "https://sru.k10plus.de/" + SRU_DATABASE
        + "?version=1.1&operation=searchRetrieve&query="
        + QUERY_PLACEHOLDER + "&maximumRecords=" + BATCH_SIZE + "&recordSchema=mods36";
    private static final String PICA_IMPORT_SYNTAX
        = "import by pica query with {0} and start {1} and status {2} and filter {3}";

    private static final String PICA_IMPORT_SYNTAX_WITH_KEY
        = "import by pica query with {0} and start {1} and status {2} and filter {3} key {4}";

    private static final String ENRICH_PPN_SYNTAX = "enrich ppn {0} with status {1} and filter {2}";

    private static final String ENRICH_PPN_SYNTAX_WITH_KEY = "enrich ppn {0} with status {1} and filter {2} key {3}";

    @MCRCommand(syntax = "schedule pica import for query {0} and filter {1}", help = "i", order = 6)
    public static void schedule(String q, String filter) {
        LOGGER.info("Creating import job for query {} and status imported and filter {}", q, filter);
        MCRJob job = new MCRJob(ThUniBibImportJobAction.class);
        job.setParameter("query", q);
        job.setParameter("status", "imported");
        job.setParameter("filter", filter);
        MCRJobQueueManager.getInstance().getJobQueue(ThUniBibImportJobAction.class).offer(job);
    }

    @MCRCommand(syntax = PICA_IMPORT_SYNTAX_WITH_KEY, help = "imports all objects for a specific query", order = 5)
    public static List<String> importByPicaQueryWithKey(String picaQuery, String startStr, String status,
        String filterTransformer, String importId) {

        final String request = buildRequestURL(picaQuery, startStr);
        final Element result = Objects.requireNonNull(MCRURIResolver.instance().resolve(request));

        XPathExpression<Element> r = XPathFactory.instance()
            .compile(".//mods:mods/mods:recordInfo/mods:recordIdentifier",
                Filters.element(), null, MCRConstants.ZS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
        List<Element> recordIdentifierElements = r.evaluate(result);

        final String numberOfRecordsStr = result.getChildTextNormalize("numberOfRecords", MCRConstants.ZS_NAMESPACE);

        if (numberOfRecordsStr == null) {
            LOGGER.warn("Could not get '<numberOfRecords/>' element");
            return new ArrayList<>();
        }

        final int numberOfRecords = Integer.parseInt(numberOfRecordsStr);
        final int start = Integer.parseInt(startStr);

        TRACKER.trackSize(importId, numberOfRecords);

        List<String> commands = new ArrayList<>(recordIdentifierElements.size());
        recordIdentifierElements.stream().map(Element::getTextNormalize).forEach(ppn -> {
            TRACKER.track(importId, ppn);
            commands.add(
                ENRICH_PPN_SYNTAX_WITH_KEY.replace("{0}", ppn)
                    .replace("{1}", status)
                    .replace("{2}", filterTransformer)
                    .replace("{3}", importId));
        });

        if ((start + BATCH_SIZE) < numberOfRecords) {
            commands.add(PICA_IMPORT_SYNTAX_WITH_KEY.replace("{0}", picaQuery)
                .replace("{1}", "" + (start + BATCH_SIZE))
                .replace("{2}", status)
                .replace("{3}", filterTransformer)
                .replace("{4}", importId));
        }

        return commands;
    }

    @MCRCommand(syntax = PICA_IMPORT_SYNTAX, help = "imports all objects for a specific query", order = 5)
    public static List<String> importByPicaQuery(String picaQuery, String startStr, String status,
        String filterTransformer) {
        String importId = UUID.randomUUID().toString();
        return importByPicaQueryWithKey(picaQuery, startStr, status, filterTransformer, importId);
    }

    @MCRCommand(syntax = ENRICH_PPN_SYNTAX_WITH_KEY, help = "Imports document with ppn and enrichment resolver",
        order = 1)
    public static MCRObject enrichOrCreateByPPNWithKey(String ppnID, String status, String filterTransformer,
        String importId) {

        // 2. check against Solr (duplicates across multiple imports)
        String ppn_field = "id_" + PPN_IDENTIFIER;
        MCRObject mcrObject = null;

        if (isAlreadyStored(ppn_field, ppnID)) {
            LOGGER.info("Found duplicate in Solr by field {} and value {}", ppn_field, ppnID);
            TRACKER.decrementTrackSize(importId);
        } else {
            try {
                MCRMODSWrapper wrappedMods = createMinimalMods(ppnID);
                new MCREnrichmentResolver().enrichPublication(wrappedMods.getMODS(), "import");
                LOGGER.debug(
                    new XMLOutputter(Format.getPrettyFormat()).outputString(wrappedMods.getMCRObject().createXML()));
                wrappedMods = transform(wrappedMods, filterTransformer);
                mcrObject = createOrUpdate(wrappedMods, status);
                TRACKER.untrack(importId, ppnID, mcrObject);
            } catch (Exception e) {
                LOGGER.error("Error while creating mcr object from {}", ppnID, e);
                TRACKER.decrementTrackSize(importId);
            }
        }

        if (TRACKER.isDone(importId)) {
            List<MCRObject> objects = TRACKER.getItems(importId);
            TRACKER.clear(importId);
            // notify via e-mail
            try {
                ThUniBibMailer.sendMail(importId, objects, status, SRU_DATABASE.toUpperCase());
            } catch (Exception e) {
                LOGGER.error("Could not send email for import job {}", importId, e);
            }
        }

        return mcrObject;
    }

    @MCRCommand(syntax = ENRICH_PPN_SYNTAX, help = "Imports document with ppn and enrichment resolver", order = 2)
    public static MCRObject enrichOrCreateByPPN(String ppnID, String status, String filterTransformer) {
        String importId = UUID.randomUUID().toString();
        return enrichOrCreateByPPNWithKey(ppnID, status, filterTransformer, importId);
    }

    public static MCRMODSWrapper transform(MCRMODSWrapper mods, String transformerId)
        throws IOException, JDOMException, SAXException {

        Document xml = mods.getMCRObject().createXML();
        MCRJDOMContent tt = new MCRJDOMContent(xml);
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerId);
        MCRContent transform = transformer.transform(tt);
        return new MCRMODSWrapper(new MCRObject(transform.asXML()));
    }

    public static String buildRequestURL(String query, String start) {
        return PICA_URL.replace(QUERY_PLACEHOLDER, query) + "&startRecord=" + start;
    }

    private static String getImportID() {
        return ID_BUILDER.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime()));
    }

    @MCRCommand(syntax = "import by affiliation with GND {0} and status {1}",
        help = "test import of publications by affiliation GND, imported documents get status one of 'confirmed', " +
            "'submitted', 'imported'",
        order = 10
    )
    public static void importByAffiliation(String affiliationGND, String import_status) {
        final List<String> allowedStatus = Arrays.asList(new String[] { "confirmed", "submitted", "imported" });

        // HashSets for finding duplicates during the import session, PPN is hard wired, rest is configurable
        Map<String, Set<String>> duplicateCheckSetsMap = setUpDuplicateCheckSets();

        if (allowedStatus.contains(import_status)) {

            List<String> affiliatedPersonsGNDs = getAllGNDsOfAffiliatedPersons(affiliationGND);
            for (String affiliatedPersonGND : affiliatedPersonsGNDs) {
                Map<String, Document> ppnToPublicationMap = getAllPublicationsOfPersonByGND(affiliatedPersonGND);
                for (String ppnID : ppnToPublicationMap.keySet()) {
                    if (!checkForDuplicates(ppnID, ppnToPublicationMap.get(ppnID), duplicateCheckSetsMap)) {
                        MCRMODSWrapper wrappedMods = createMinimalMods(ppnID);
                        new MCREnrichmentResolver().enrichPublication(wrappedMods.getMODS(), "import");
                        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(
                            wrappedMods.getMCRObject().createXML()));
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
        for (String checkID : DUPLICATE_CHECK_IDS) {
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
        if (duplicateCheckSetsMap.get(PPN_IDENTIFIER).contains(publication_ppn)) {
            LOGGER.info("Found duplicate in import session by PPN {}", publication_ppn);
            return true;
        } else {
            duplicateCheckSetsMap.get(PPN_IDENTIFIER).add(publication_ppn);
        }
        // 2. check against Solr (duplicates across multiple imports)
        String ppn_field = "id_" + PPN_IDENTIFIER;
        if (isAlreadyStored(ppn_field, publication_ppn)) {
            LOGGER.info("Found duplicate in Solr by field {} and value {}", ppn_field, publication_ppn);
            return true;
        }

        // Check for duplicates using configured IDs
        // 1. Check HashSets (duplicates per import session)
        for (String checkID : DUPLICATE_CHECK_IDS) {
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

    public static boolean isAlreadyStored(String identifierField, String identifierValue) {
        LOGGER.info("Check if already stored in Solr using field {} and value {}", identifierField, identifierValue);
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(identifierField + ":" + MCRSolrUtils.escapeSearchValue(identifierValue));
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
            Filters.element(), null, MCRConstants.ZS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
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
                Filters.element(), null, MCRConstants.ZS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
            List<Element> publicationElements = modsRecordExpr.evaluate(searchResponseXML);
            LOGGER.info("Got {} publications for Person by GND {}", publicationElements.size(), gnd);

            for (Element publication : publicationElements) {
                // get the PPN of each publication and map it
                XPathExpression<Element> recordIdentifierExpr = xFactory.compile("//mods:recordIdentifier",
                    Filters.element(), null, MCRConstants.ZS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
                Element recordIdentifierElement = recordIdentifierExpr.evaluateFirst(publication);
                if (recordIdentifierElement != null) {
                    String ppn = recordIdentifierElement.getValue();
                    publication.detach();
                    ppnToPublicationMap.put(ppn, new Document(publication));
                } else {
                    LOGGER.error("Publication without PPN found for GND {}, skipping", gnd);
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
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
            LOGGER.error("", e);
            throw new RuntimeException(e);
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
        for (JsonElement element : jsonObject.getAsJsonArray("member")) {
            JsonObject member = element.getAsJsonObject();
            String gndIdentifier = member.get("gndIdentifier").getAsString();
            affiliatedPersonsGNDs.add(gndIdentifier);
        }
        LOGGER.debug("Got the following GNDs of affiliated persons with affiliation GND {}: ", affiliatedPersonsGNDs);
        return affiliatedPersonsGNDs;
    }

    private static MCRObjectID nextFreeID() {
        return MCRMetadataManager.getMCRObjectIDGenerator().getNextFreeId(projectID, "mods");
    }

    private static MCRObject createOrUpdate(MCRMODSWrapper wrappedMCRobj, String import_status) {
        wrappedMCRobj.setServiceFlag("importID", "SRU-PPN-" + getImportID());
        MCRObject object = wrappedMCRobj.getMCRObject();
        // save object
        try {
            setState(wrappedMCRobj, import_status);
            if (MCRMetadataManager.exists(object.getId())) {
                LOGGER.info("Update object {}!", object.getId().toString());
                MCRMetadataManager.update(object);
            } else {
                LOGGER.info("Create object {}!", object.getId().toString());
                MCRMetadataManager.create(object);
            }

            return object;
        } catch (MCRAccessException e) {
            throw new MCRException("Error while creating " + object.getId().toString(), e);
        }
    }

    private static void setState(MCRMODSWrapper wrapped, String import_status) {
        wrapped.setServiceFlag("status", import_status);
    }
}
