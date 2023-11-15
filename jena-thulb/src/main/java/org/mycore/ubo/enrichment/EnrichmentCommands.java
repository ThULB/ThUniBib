package org.mycore.ubo.enrichment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfigurationDir;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.enrichment.MCREnrichmentResolver;
import org.mycore.oai.pmh.CannotDisseminateFormatException;
import org.mycore.oai.pmh.IdDoesNotExistException;
import org.mycore.oai.pmh.harvester.Harvester;
import org.mycore.oai.pmh.harvester.HarvesterBuilder;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@MCRCommandGroup(name = "experimental DBT import commands")
public class EnrichmentCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(EnrichmentCommands.class);

    private static final String projectID = "ubo";

    private static final String OAILAST_HARVEST_TXT = "OAILastHarvest.txt";
    private static final Date EARLY_DATE = new Date(1);

    @MCRCommand(syntax = "test import dbt with status {0}",
        help = "test DBT import of mods from OAI, imported documents get status one of 'confirmed', " +
            "'submitted', 'imported'",
        order = 10
    )
    public static void testWithStatus(String import_status) {
        final List<String> allowedStatus = Arrays.asList(new String[] { "confirmed", "submitted", "imported" });
        if (allowedStatus.contains(import_status)) {
            testSub(import_status);
        } else {
            LOGGER.info("Status not allowed: {}, use one of {}", import_status, String.join(", ", allowedStatus));
        }
    }

    @MCRCommand(syntax = "test import dbt",
        help = "tests DBT import of mods from OAI",
        order = 20)
    public static void test() throws IdDoesNotExistException, CannotDisseminateFormatException {
        testSub("imported");
    }

    @MCRCommand(syntax = "test single import dbt with status {0} and oai identifier {1}",
        help = "test DBT import of single mods document from OAI, imported documents get status one of " +
            "'confirmed', 'submitted', 'imported'; OAI Identifier typically has the following format: " +
            "oai:www.db-thueringen.de:dbt_mods_00012345 (thueringen)",
        order = 30
    )
    public static void testSingleWithStatus(String import_status, String oai_identifier) {
        try {
            Harvester harvester = HarvesterBuilder.createNewInstance(
                "https://www.db-thueringen.de/servlets/OAIDataProvider");

            OAIRecord oaiRecord = new OAIRecord(harvester.getRecord(oai_identifier, "mods"));
            MCRMODSWrapper wrappedObj = createMinimalMods(oaiRecord);
            new MCREnrichmentResolver().enrichPublication(wrappedObj.getMODS(), "import");
            if (filterObject(wrappedObj)) {
                wrappedObj = mapToObject(wrappedObj);
                createOrUpdate(wrappedObj, import_status);
                LOGGER.info(
                    new XMLOutputter(Format.getPrettyFormat()).outputString(wrappedObj.getMCRObject().createXML()));
            }
        } catch (CannotDisseminateFormatException e) {
            e.printStackTrace();
        } catch (IdDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    private static MCRMODSWrapper createMinimalMods(OAIRecord oaiRecord) {
        LOGGER.info("Start createMinimalMods");
        String recordID = oaiRecord.getRecord().getHeader().getId();
        LOGGER.info("ID of oaiRecord: {}", recordID);
        String dbtID = recordID.replace("oai:www.db-thueringen.de:", "");
        LOGGER.info("DBT-ID of oaiRecord: {}", dbtID);

        final Element modsRoot = new Element("mods", MCRConstants.MODS_NAMESPACE);
        final Element identifierElement = new Element("identifier", MCRConstants.MODS_NAMESPACE);
        identifierElement.setAttribute("type", "dbt");
        identifierElement.setText(dbtID);

        final MCRObject dbt;
        dbt = MCRMODSWrapper.wrapMODSDocument(modsRoot, projectID);
        dbt.setId(nextFreeID());

        final MCRMODSWrapper wrapper = new MCRMODSWrapper(dbt);
        wrapper.addElement(identifierElement);

        //LOGGER.info(new XMLOutputter(Format.getPrettyFormat()).outputString(wrapper.getMCRObject().createXML()));
        LOGGER.info("End createMinimalMods");
        return wrapper;
    }

    public static void testSub(String import_status) {
        Date lastHarvest = getLastHarvestDate();
        final Date newHarvest = new Date();

        if (lastHarvest.before(newHarvest)) {
            final RecordTransformer recordTransformer = new RecordTransformer(
                "https://www.db-thueringen.de/servlets/OAIDataProvider",
                "mods",
                "institute:1");

            recordTransformer.getAll(lastHarvest, null)
                .map(EnrichmentCommands::createMinimalMods)
                .map(obj -> {
                    new MCREnrichmentResolver().enrichPublication(obj.getMODS(), "import");
                    return obj;
                })
                .filter(obj -> filterObject(obj))
                .map(EnrichmentCommands::mapToObject)
                .forEach(obj -> EnrichmentCommands.createOrUpdate(obj, import_status));

            saveLastHarvestDate(newHarvest);
        }
    }

    private static boolean filterObject(MCRMODSWrapper wrappedMCRObj) {
        boolean goesTroughFilter = true;
        final List<String> filteredGenres = Arrays.asList(new String[] { "video", "audio", "picture", "broadcasting" });

        String mcrID = wrappedMCRObj.getMCRObject().getId().toString();
        Element modsRootElement = wrappedMCRObj.getMODS();

        //LOGGER.info(new XMLOutputter(Format.getPrettyFormat()).outputString(recordRootElement));
        LOGGER.info("Deciding whether to filter: {}", mcrID);

        final XPathExpression<Element> xpath = XPathFactory.instance()
            .compile("mods:genre",
                Filters.element(), null, MCRConstants.MODS_NAMESPACE);
        final Element genreElem = xpath.evaluateFirst(modsRootElement);

        if (genreElem != null) {
            String genre = genreElem.getText();
            LOGGER.info("Genre is: {}", genre);
            if (filteredGenres.contains(genre)) {
                goesTroughFilter = false;
                LOGGER.info("filtered obj: {}, because genre is one of: {}", mcrID, filteredGenres);
            }
        } else {
            goesTroughFilter = false; // do not create/update objects that have no genre (as they are most likely empty
            // at this point
            LOGGER.info("filtered obj: {}, because no genre found", mcrID);
        }
        return goesTroughFilter;
    }

    private static void saveLastHarvestDate(Date lastHarvest) {
        final Path lastHarvestDateFilePath = getLastHarvestDateFilePath();
        final String dateAsString = new SimpleDateFormat(RecordTransformer.OAI_SIMPLE_FORMAT).format(lastHarvest);
        try {
            Files.write(lastHarvestDateFilePath, dateAsString.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE);
        } catch (IOException e) {
            LOGGER.error("Error while writing lastHarvestDate", e);
        }
    }

    private static Date getLastHarvestDate() {
        final Path lastHarvestFile = getLastHarvestDateFilePath();

        if (Files.exists(lastHarvestFile)) {
            try {
                return Files.readAllLines(lastHarvestFile).stream().findFirst().map(str -> {
                    try {
                        return new SimpleDateFormat(RecordTransformer.OAI_SIMPLE_FORMAT).parse(str);
                    } catch (ParseException e) {
                        LOGGER.error("Error while parsing date " + str, e);
                        return null;
                    }
                }).orElse(EARLY_DATE);
            } catch (IOException e) {
                LOGGER.error("Error while reading date of last harvest from " + lastHarvestFile.toString(), e);

            }
        } else {
            LOGGER.info("{} not found. Assume last harvest was {}", OAILAST_HARVEST_TXT, EARLY_DATE.toString());
        }
        return EARLY_DATE;
    }

    private static Path getLastHarvestDateFilePath() {
        final Path configPath = MCRConfigurationDir.getConfigurationDirectory().toPath();
        return configPath.resolve(OAILAST_HARVEST_TXT);
    }

    private static MCRMODSWrapper mapToObject(MCRMODSWrapper wrappedMCRObj) {
        final Element modsRoot = wrappedMCRObj.getMODS();
        final XPathExpression<Element> xpath = XPathFactory.instance()
            .compile("mods:identifier[@type='dbt']",
                Filters.element(), null, MCRConstants.MODS_NAMESPACE);
        final Element dbtIdentifierElem = xpath.evaluateFirst(modsRoot);

        String dbtIdentifier = "";
        if (dbtIdentifierElem != null) {
            dbtIdentifier = dbtIdentifierElem.getText();
        }

        final String solrQuery = "id_dbt:\"" + dbtIdentifier + "\"";
        LOGGER.info("Got dbt identifier: {}, using solr query: {}", dbtIdentifier, solrQuery);
        SolrDocument first = null;
        try {
            first = MCRSolrSearchUtils
                .first(MCRSolrClientFactory.getMainSolrClient(), solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        final MCRObject dbt;
        if (first == null) {
            dbt = MCRMODSWrapper.wrapMODSDocument(modsRoot, projectID);
            dbt.setId(nextFreeID());
        } else {
            final String id = (String) first.getFieldValue("id");
            LOGGER.info("Found object to update: " + id);
            final MCRObjectID objectID = MCRObjectID.getInstance(id);
            dbt = MCRMetadataManager.retrieveMCRObject(objectID);
            new MCRMODSWrapper(dbt).setMODS(modsRoot);
        }

        return new MCRMODSWrapper(dbt);
    }

    private static MCRObjectID nextFreeID() {
        return MCRMetadataManager.getMCRObjectIDGenerator().getNextFreeId(projectID, "mods");
    }

    private static void createOrUpdate(MCRMODSWrapper wrappedMCRobj, String import_status) {

        MCRObject object = wrappedMCRobj.getMCRObject();
        // save object
        try {
            setState(wrappedMCRobj, import_status);

            final Optional<MCRObjectID> alreadyExists = Optional.of(object.getId())
                .filter(MCRMetadataManager::exists);
            if (!alreadyExists.isPresent()) {
                LOGGER.info("Create object {}!", object.getId().toString());
                MCRMetadataManager.create(object);
            } else {
                LOGGER.info("Update object {}!", object.getId().toString());
                MCRMetadataManager.update(object);
            }
        } catch (MCRAccessException e) {
            throw new MCRException("Error while creating " + object.getId().toString(), e);
        }
    }

    private static void setState(MCRMODSWrapper wrapped, String import_status) {
        wrapped.setServiceFlag("status", import_status);
    }
}
