package de.uni_jena.thunibib.his.xml;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.cli.HISinOneCommands;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * This resolver allows resolving of internal keys used by HISinOne to address its entities.
 * </p>
 *
 * Usage
 * <p>
 * <code>hisinone:&lt;resolve|create&gt;:&lt;[requested field]&gt;:&lt;conference|creatorType|documentType|journal|publication|publicationAccessType|publicationResource|publicationType|globalIdentifiers|language|peerReviewed|person|publisher|researchAreaKdsf|subjectArea|state|thesisType|visibility&gt;:[value]</code>
 * </p>
 *
 * Note
 * <p>
 * The <strong><code>create:</code></strong> uri part is supported for <strong><code>publisher:</code></strong> uri part only.
 * </p>
 *
 * @author shermann (Silvio Hermann)
 * */
public class HISinOneResolver implements URIResolver {
    private static final Logger LOGGER = LogManager.getLogger(HISinOneResolver.class);

    private static final Map<String, SysValue.LanguageValue> LANGUAGE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CONFERENCE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CREATOR_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> DOCUMENT_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> IDENTIFIER_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PEER_REVIEWED_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_ACCESS_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_RESOURCE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLISHER_MAP = new HashMap<>();
    private static final Map<String, SysValue> RESEARCH_AREA_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> STATE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> SUBJECT_AREA_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> THESIS_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> VISIBILITY_TYPE_MAP = new HashMap<>();

    public enum Mode {
        resolve, create
    }

    public enum ResolvableTypes {
        conference,
        creatorType,
        documentType,
        globalIdentifiers,
        journal,
        language,
        peerReviewed,
        person,
        publication,
        publicationAccessType,
        publicationResource,
        publicationType,
        publisher,
        researchAreaKdsf,
        state,
        subjectArea,
        thesisType,
        visibility
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.debug("Resolving '{}'", href);

        String[] parts = href.split(":");

        Mode mode = Mode.valueOf(parts[1]);
        String field = parts[2];
        String entity = parts[3];
        String fromValue;

        if (ResolvableTypes.publicationType.name().equals(entity) || ResolvableTypes.documentType.name()
            .equals(entity)) {
        }

        String idValue = null;
        if (ResolvableTypes.person.name().equals(entity)) {
            idValue = parts[5];
        }

        fromValue = parts.length > 4 ? URLDecoder.decode(parts[4], StandardCharsets.UTF_8) : "";

        var sysValue = switch (ResolvableTypes.valueOf(entity)) {
            case conference -> resolveConference(fromValue);
            case creatorType -> resolveCreatorType(fromValue);
            case documentType -> resolveDocumentType(fromValue);
            case globalIdentifiers -> resolveIdentifierType(fromValue);
            case journal -> Mode.resolve.equals(mode) ? resolveJournal(fromValue) : createParent(fromValue);
            case language -> resolveLanguage(fromValue);
            case peerReviewed -> resolvePeerReviewedType(fromValue);
            case person -> resolvePerson(fromValue, idValue);
            case publication -> Mode.resolve.equals(mode) ? resolvePublication(fromValue) : createParent(fromValue);
            case publicationAccessType -> resolvePublicationAccessType(fromValue);
            case publicationResource -> resolvePublicationResourceType(fromValue);
            case publicationType -> resolvePublicationType(fromValue);
            case publisher -> Mode.resolve.equals(mode) ? resolvePublisher(fromValue) : createPublisher(fromValue);
            case researchAreaKdsf -> resolveResearchAreaKdsf(fromValue);
            case state -> resolveState(fromValue);
            case subjectArea -> resolveSubjectArea(fromValue);
            case thesisType -> resolveThesisType(fromValue);
            case visibility -> resolveVisibility(fromValue);
        };

        int resolvedValue = getFieldValue(sysValue, field);
        LOGGER.info("Resolved {} to {}", href, resolvedValue);
        return new JDOMSource(new Element("int").setText(String.valueOf(resolvedValue)));
    }

    private SysValue resolveConference(String value) {
        if (CONFERENCE_TYPE_MAP.containsKey(value)) {
            return CONFERENCE_TYPE_MAP.get(value);
        }

        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
        String[] conferenceParts = decodedValue.split(";");

        if (!(conferenceParts.length >= 3)) {
            return SysValue.UnresolvedSysValue;
        }

        String name = conferenceParts[0].trim();
        String location = conferenceParts[1].trim();
        long year = toEpochMilli(conferenceParts[2].trim());

        // Search by name of the conference
        Map<String, String> params = new HashMap<>();
        params.put("q", conferenceParts[0]);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Conference.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Conference.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Conference[] conferences = response.readEntity(SysValue.Conference[].class);
            Optional<SysValue.Conference> match = Arrays.stream(conferences)
                .filter(conference -> conference.getDefaultText().equals(name))
                .filter(conference -> location.equals(conference.getCity()))
                .filter(conference -> conference.getStartDate() >= year && conference.getEndDate() <= year)
                .findFirst();
            return match.isPresent() ? match.get() : SysValue.UnresolvedSysValue;
        }
    }

    private long toEpochMilli(String conferenceYear) {
        Instant instant = Instant.parse(conferenceYear + "-01-01T00:00:00Z");
        return instant.minus(1, ChronoUnit.HOURS).toEpochMilli();
    }

    /**
     * Resolves a person by a given identifier and the type of the identifier.
     *
     * @param type the type of the identifier
     * @param value the value of the identifier
     *
     * @return {@link SysValue}
     */
    protected SysValue resolvePerson(String type, String value) {
        Map<String, String> parameter = new HashMap<>();
        parameter.put(SysValue.PersonIdentifier.getTypeParameterName(), type);
        parameter.put(SysValue.PersonIdentifier.getValueParameterName(), value);
        String path = SysValue.resolve(SysValue.PersonIdentifier.class);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.post(path, null, parameter)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, path);
                return SysValue.ErroneousSysValue;
            }

            SysValue.PersonIdentifier sysValue = response.readEntity(SysValue.PersonIdentifier.class);
            return sysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    protected SysValue resolveJournal(String fromValue) {
        if (!exists(fromValue)) {
            return SysValue.UnresolvedSysValue;
        }

        MCRObject host = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(fromValue));
        if (host.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisId = host.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Journal.class) + "/" + hisId)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Journal.class));
                return SysValue.ErroneousSysValue;
            }
            SysValue.Journal journal = response.readEntity(SysValue.Journal.class);
            return journal;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Resolves the <code>publicationResourceType</code> by the given <code>mods:typeofResource</code> category id.
     *
     * @param resourceTypeText
     * @return the resolved his id or the HIS id for default value "Sonstige Darstellungsform"
     */
    private SysValue resolvePublicationResourceType(String resourceTypeText) {
        if (PUBLICATION_RESOURCE_TYPE_MAP.containsKey(resourceTypeText)) {
            return PUBLICATION_RESOURCE_TYPE_MAP.get(resourceTypeText);
        }

        Optional<MCRLabel> label = Optional.empty();
        try {
            MCRCategory category = MCRCategoryDAOFactory
                .getInstance()
                .getCategory(MCRCategoryID.fromString("typeOfResource:" + resourceTypeText), 0);
            label = category.getLabel("x-mapping-his-pub-resource-value");
        } catch (Exception ex) {
            LOGGER.warn("Could not resolve label x-mapping-his-pub-resource-value for category {}",
                ("typeOfResource:" + resourceTypeText), ex);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationResourceValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationResourceValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationResourceValue> availableResourceTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationResourceValue>>() {
                });

            // find his key of resourceType
            if (label.isPresent()) {
                String text = label.get().getText();
                Optional<SysValue.PublicationResourceValue> resolved = availableResourceTypes
                    .stream()
                    .filter(t -> text.equals(t.getDefaultText()))
                    .findFirst();

                if (resolved.isPresent()) {
                    return resolved.get();
                }
            }

            // return default his resource type
            return availableResourceTypes
                .stream()
                .filter(t -> "Sonstige Darstellungsform".equals(t.getDefaultText()))
                .findFirst().get();
        }
    }

    protected SysValue createParent(String mcrid) {
        return HISinOneCommands.publish(mcrid);
    }

    /**
     * Resolves the {@link SysValue} with id in HISinOne of the given mycoreobject id.
     *
     * @param mcrid the mcrid of a publication
     *
     * @return a {@link SysValue} with for the publication of the given mycoreobject id or {@link SysValue#UnresolvedSysValue}
     */
    private SysValue resolvePublication(String mcrid) {
        if (!exists(mcrid)) {
            return SysValue.UnresolvedSysValue;
        }

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisid = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Publication.class) + "/" + hisid)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Publication.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Publication publication = response.readEntity(SysValue.Publication.class);
            return publication;
        }
    }

    private SysValue resolvePublisher(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        Map<String, String> params = new HashMap<>();
        params.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublisherWrappedValueSearch.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublisherWrappedValueSearch.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublisherWrappedValueSearch> publishers = response.readEntity(
                new GenericType<List<SysValue.PublisherWrappedValueSearch>>() {
                });

            List<SysValue.PublisherWrappedValueSearch> resultList = publishers.stream()
                .filter(pwv -> decodedValue.equals(pwv.getUniqueName()))
                .toList();

            SysValue r = !resultList.isEmpty() ? resultList.get(0) : SysValue.UnresolvedSysValue;
            if (r instanceof SysValue.PublisherWrappedValueSearch) {
                PUBLISHER_MAP.put(decodedValue, r);
            }
            return r;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Creates a new publisher. Default language is <em>German</em> and default place is <em>unknown/unbekannt</em>.
     * */
    private SysValue createPublisher(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        SysValue.LanguageValue languageValue = (SysValue.LanguageValue) resolveLanguage("de");

        JsonObject language = new JsonObject();
        language.addProperty("id", languageValue.getId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("defaulttext", decodedValue);
        jsonObject.addProperty("uniquename", decodedValue);
        jsonObject.add("language", language);
        jsonObject.addProperty("place", "unbekannt");

        try (HISInOneClient hisClient = HISinOneClientFactory.create();

            Response response = hisClient.post(SysValue.resolve(SysValue.PublisherWrappedValueCreate.class),
                jsonObject.toString())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublisherWrappedValueCreate.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.PublisherWrappedValueCreate publisher = response.readEntity(
                SysValue.PublisherWrappedValueCreate.class);
            return publisher;
        }
    }

    /**
     * Resolves the his-id by the given peerreviewed category id.
     *
     * @param peerReviewedCategId the categ id category 'peerreviewed';
     *
     * @return {@link SysValue}
     * */
    private SysValue resolvePeerReviewedType(String peerReviewedCategId) {
        if (PEER_REVIEWED_TYPE_MAP.containsKey(peerReviewedCategId)) {
            return PEER_REVIEWED_TYPE_MAP.get(peerReviewedCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PeerReviewedValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PeerReviewedValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PeerReviewedValue> prTypes = response.readEntity(
                new GenericType<List<SysValue.PeerReviewedValue>>() {
                });

            var text = switch (peerReviewedCategId) {
                case "true" -> "ja";
                case "false" -> "nein";
                default -> "keine Angabe";
            };

            Optional<SysValue.PeerReviewedValue> peerReviewedValue = prTypes
                .stream()
                .filter(t -> text.equals(t.getUniqueName()))
                .findFirst();
            if (peerReviewedValue.isPresent()) {
                PEER_REVIEWED_TYPE_MAP.put(peerReviewedCategId, peerReviewedValue.get());
                return peerReviewedValue.get();
            }
        }

        return SysValue.UnresolvedSysValue;
    }

    /**
     * Resolves the his-id by the given research area id.
     *
     * @param accessRightsCategId the id of the category Access Rights - KDSF
     *
     * @return {@link SysValue}
     * */
    private SysValue resolvePublicationAccessType(String accessRightsCategId) {
        if (PUBLICATION_ACCESS_TYPE_MAP.containsKey(accessRightsCategId)) {
            return PUBLICATION_ACCESS_TYPE_MAP.get(accessRightsCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationAccessTypeValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationAccessTypeValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationAccessTypeValue> accessTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationAccessTypeValue>>() {
                });

            MCRCategoryID mcrCategoryID = MCRCategoryID.fromString("accessrights:" + accessRightsCategId);
            MCRCategory mcrCategory = MCRCategoryDAOFactory.getInstance().getCategory(mcrCategoryID, 1);

            Optional<MCRLabel> label = mcrCategory.getLabel("en");

            if (!label.isPresent()) {
                return SysValue.UnresolvedSysValue;
            }

            String text = label.get().getText().toLowerCase(Locale.ROOT);
            Optional<SysValue.PublicationAccessTypeValue> first = accessTypes
                .stream()
                .filter(patv -> text.equals(patv.getDefaultText().toLowerCase(Locale.ROOT)))
                .findFirst();

            if (first.isEmpty()) {
                return SysValue.UnresolvedSysValue;
            }

            PUBLICATION_ACCESS_TYPE_MAP.put(accessRightsCategId, first.get());
            return first.get();
        }
    }

    /**
     * Resolves the his-id by the given research area id.
     *
     * @param areaCategId the id of the research area
     *
     * @return the id of that research area in HISinOne
     * */
    private SysValue resolveResearchAreaKdsf(String areaCategId) {
        if (RESEARCH_AREA_TYPE_MAP.containsKey(areaCategId)) {
            return RESEARCH_AREA_TYPE_MAP.get(areaCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ResearchAreaKdsfValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ResearchAreaKdsfValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.ResearchAreaKdsfValue> availableTypes = response.readEntity(
                new GenericType<List<SysValue.ResearchAreaKdsfValue>>() {
                });

            Optional<SysValue.ResearchAreaKdsfValue> raKdsfValue = availableTypes
                .stream()
                .filter(v -> areaCategId.equals(v.getUniqueName()))
                .findFirst();

            if (raKdsfValue.isPresent()) {
                RESEARCH_AREA_TYPE_MAP.put(areaCategId, raKdsfValue.get());
                return raKdsfValue.get();
            }
        }

        return SysValue.UnresolvedSysValue;
    }

    /**
     * Resolves the HISinOne id of the given identifier.
     *
     * @param identifierType the type of the identifier, like doi, urn, ...
     *
     * @return the SysValue containing the id of that identifier.
     * */
    private SysValue resolveIdentifierType(String identifierType) {
        if (IDENTIFIER_TYPE_MAP.containsKey(identifierType)) {
            return IDENTIFIER_TYPE_MAP.get(identifierType);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.GlobalIdentifierType.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.GlobalIdentifierType.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.GlobalIdentifierType> availableTypes = response.readEntity(
                new GenericType<List<SysValue.GlobalIdentifierType>>() {
                });

            Optional<SysValue.GlobalIdentifierType> type = availableTypes
                .stream()
                .filter(t -> identifierType.toUpperCase(Locale.ROOT).equals(t.getUniqueName().toUpperCase(Locale.ROOT)))
                .findFirst();

            if (type.isPresent()) {
                IDENTIFIER_TYPE_MAP.put(identifierType, type.get());
                return type.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    protected SysValue resolvePublicationType(String fromXpathMapping) {
        if (PUBLICATION_TYPE_MAP.containsKey(fromXpathMapping)) {
            return PUBLICATION_TYPE_MAP.get(fromXpathMapping);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationTypeValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationTypeValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationTypeValue> pubTypeValues = response.readEntity(
                new GenericType<List<SysValue.PublicationTypeValue>>() {
                });

            String expectedType = MCRXMLFunctions.getDisplayName("kdsfPublicationType", fromXpathMapping, "de");

            Optional<SysValue.PublicationTypeValue> tpv = pubTypeValues
                .stream()
                .filter(pubType -> pubType.getUniqueName().equals(expectedType))
                .findFirst();

            PUBLICATION_TYPE_MAP.put(fromXpathMapping, tpv.get());
            return tpv.get();
        }
    }

    /**
     * Determines the documentType on base of fromXpathMapping/publicationType. Is currently fixed to 'Bibliographie'
     * */
    private SysValue resolveDocumentType(String fromXpathMapping) {
        if (DOCUMENT_TYPE_MAP.containsKey(fromXpathMapping)) {
            return DOCUMENT_TYPE_MAP.get(fromXpathMapping);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response bookResp = hisClient.get(SysValue.resolve(SysValue.DocumentTypeBook.class));
            Response articleResp = hisClient.get(SysValue.resolve(SysValue.DocumentTypeArticle.class));) {

            if (bookResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(bookResp, SysValue.resolve(SysValue.DocumentTypeBook.class));
                return SysValue.ErroneousSysValue;
            }
            if (articleResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(articleResp, SysValue.resolve(SysValue.DocumentTypeArticle.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.DocumentType> ofBook = bookResp.readEntity(new GenericType<List<SysValue.DocumentType>>() {
            });
            ofBook.addAll(articleResp.readEntity(new GenericType<List<SysValue.DocumentType>>() {
            }));

            // remove duplicates
            List<SysValue.DocumentType> list = ofBook
                .stream()
                .collect(Collectors
                    .toMap(SysValue.DocumentType::getId, existing -> existing, (existing, replace) -> existing))
                .values()
                .stream()
                .toList();

            String documentTypeName = MCRXMLFunctions.getDisplayName("kdsfDocumentType", fromXpathMapping, "de");
            Optional<SysValue.DocumentType> documentType = list
                .stream()
                .filter(v -> v.getDefaultText().equals(documentTypeName))
                .findFirst();

            if (documentType.isPresent()) {
                DOCUMENT_TYPE_MAP.put(fromXpathMapping, documentType.get());
                return documentType.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    private SysValue resolveThesisType(String ubogenre) {
        if (THESIS_TYPE_MAP.containsKey(ubogenre)) {
            return THESIS_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.QualificationThesisValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.QualificationThesisValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.QualificationThesisValue> thesisValues = response.readEntity(
                new GenericType<List<SysValue.QualificationThesisValue>>() {
                });

            MCRCategoryID categId = MCRCategoryID.fromString("ubogenre:" + ubogenre);
            MCRCategoryID thesisCategId = MCRCategoryID.fromString("ubogenre:thesis");
            List<MCRCategory> children = MCRCategoryDAOFactory.getInstance().getChildren(thesisCategId);
            boolean isThesis = children.stream().filter(c -> c.getId().equals(categId)).findAny().isPresent();

            SysValue.QualificationThesisValue sysValue = null;
            if (isThesis) {
                String text = MCRCategoryDAOFactory
                    .getInstance()
                    .getCategory(categId, -1).getLabel("de").get()
                    .getText();

                Optional<SysValue.QualificationThesisValue> qtv = thesisValues.stream()
                    .filter(tv -> text.equals(tv.getDefaultText())).findFirst();

                if (qtv.isPresent()) {
                    sysValue = qtv.get();
                }
            } else {
                sysValue = thesisValues.stream()
                    .filter(tv -> "nicht zutreffend".equals(tv.getDefaultText())).findFirst().get();
            }
            THESIS_TYPE_MAP.put(ubogenre, sysValue);
            return sysValue;
        }
    }

    private SysValue resolveSubjectArea(String destatisId) {
        if (SUBJECT_AREA_TYPE_MAP.containsKey(destatisId)) {
            return SUBJECT_AREA_TYPE_MAP.get(destatisId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.SubjectAreaValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.SubjectAreaValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.SubjectAreaValue> subjectAreas = response.readEntity(
                new GenericType<List<SysValue.SubjectAreaValue>>() {
                });

            Optional<SysValue.SubjectAreaValue> areaValue = subjectAreas.stream()
                .filter(subjectAreaValue -> destatisId.equals(subjectAreaValue.getUniqueName()))
                .findFirst();

            if (areaValue.isPresent()) {
                SUBJECT_AREA_TYPE_MAP.put(destatisId, areaValue.get());
                return areaValue.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    protected SysValue resolveCreatorType(String value) {
        if (CREATOR_TYPE_MAP.containsKey(value)) {
            return CREATOR_TYPE_MAP.get(value);
        }

        String path = SysValue.resolve(SysValue.PublicationCreatorTypeValue.class);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(path)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, path);
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationCreatorTypeValue> creatorTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationCreatorTypeValue>>() {
                });

            var id = switch (value) {
                default -> creatorTypes.stream()
                    .filter(state -> "Autor/-in".equals(state.getUniqueName()))
                    .findFirst()
                    .get();
            };

            CREATOR_TYPE_MAP.put(value, id);
            return id;
        }
    }

    /**
     * Resolves the HISinOne visibiltyValue by the current hsb publication status.
     *
     * @param statusCategId the current status category id of the publication
     *
     * @return the {@link SysValue}
     * */
    protected SysValue resolveVisibility(String statusCategId) {
        if (VISIBILITY_TYPE_MAP.containsKey(statusCategId)) {
            return VISIBILITY_TYPE_MAP.get(statusCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.VisibilityValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.VisibilityValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.VisibilityValue> visState = response.readEntity(
                new GenericType<List<SysValue.VisibilityValue>>() {
                });

            var id = switch (statusCategId) {
                case "confirmed", "unchecked" ->
                    visState.stream().filter(state -> "public".equals(state.getUniqueName())).findFirst().get();
                default -> visState.stream().filter(state -> "hidden".equals(state.getUniqueName())).findFirst()
                    .get();
            };

            VISIBILITY_TYPE_MAP.put(statusCategId, id);
            return id;
        }
    }

    /**
     * Resolves the HISinOne {@link SysValue.PublicationState} by the current hsb publication status.
     *
     * @param statusCategId the current status category id of the publication
     *
     * @return the {@link SysValue}
     * */
    protected SysValue resolveState(String statusCategId) {
        if (STATE_TYPE_MAP.containsKey(statusCategId)) {
            return STATE_TYPE_MAP.get(statusCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationState.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationState.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationState> pubState = response.readEntity(
                new GenericType<List<SysValue.PublicationState>>() {
                });

            var id = switch (statusCategId) {
                case "confirmed", "unchecked" ->
                    pubState.stream().filter(state -> "validiert".equals(state.getUniqueName())).findFirst().get();
                case "review" ->
                    pubState.stream().filter(state -> "Dateneingabe".equals(state.getUniqueName())).findFirst().get();
                default ->
                    pubState.stream().filter(state -> "zur Validierung".equals(state.getUniqueName())).findFirst()
                        .get();
            };

            STATE_TYPE_MAP.put(statusCategId, id);
            return id;
        }
    }

    protected SysValue resolveLanguage(String rfc5646) {
        if (LANGUAGE_TYPE_MAP.containsKey(rfc5646)) {
            return LANGUAGE_TYPE_MAP.get(rfc5646);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.LanguageValue.class))) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.LanguageValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.LanguageValue> languageValues = response.readEntity(
                new GenericType<List<SysValue.LanguageValue>>() {
                });

            Optional<SysValue.LanguageValue> languageValue = languageValues
                .stream()
                .filter(lv -> lv.getIso6391().equals(rfc5646))
                .findFirst();

            if (languageValue.isPresent()) {
                LANGUAGE_TYPE_MAP.put(rfc5646, languageValue.get());
            }

            return languageValue.isPresent() ? languageValue.get() : SysValue.UnresolvedSysValue;
        }
    }

    /**
     * Retrieve the desired field value. If the value cannot be obtained {@link SysValue#getId()} will be returned.
     *
     * @param sysValue the SysValue
     * @param fieldName the field name
     *
     * @return the value of the requested field name or {@link SysValue#getId()}.
     */
    protected int getFieldValue(SysValue sysValue, String fieldName) {
        Class clazz = sysValue.getClass();
        Field field = null;

        /* lookup field in class hierarchy */
        while (field == null && clazz != null) {
            LOGGER.debug("Checking for field {} in {}", fieldName, clazz.getSimpleName());
            Optional<Field> f = Arrays.stream(clazz.getDeclaredFields())
                .filter(df -> df.getName().equals(fieldName)).findFirst();
            if (f.isPresent()) {
                field = f.get();
            } else {
                LOGGER.debug("Field {} could not be obtained from {}. Checking superclass {}", fieldName,
                    clazz.getSimpleName(), clazz.getSuperclass());
            }
            clazz = clazz.getSuperclass();
        }

        /* field is unresolved */
        if (field == null) {
            return SysValue.UnresolvedSysValue.getId();
        }

        /* field is resolved */
        field.setAccessible(true);
        try {
            return (int) field.get(sysValue);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
            return SysValue.ErroneousSysValue.getId();
        }
    }

    /**
     * Checks for valid {@link MCRObjectID} and if object actually exists.
     *
     * @param mcrid the id to check
     *
     * @return true if object exists, false otherwise
     * */
    protected boolean exists(String mcrid) {
        if (!MCRObjectID.isValid(mcrid)) {
            LOGGER.error("{} is not a valid {}", mcrid, MCRObjectID.class.getSimpleName());
            return false;
        }

        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            LOGGER.warn("{} does not exist", mcrid);
            return false;
        }
        return true;
    }

    /**
     * Logs the response to the error log. Closes the {@link Response} object.
     *
     * @param response the response
     */
    public void logError(Response response, String endpoint) {
        LOGGER.error("{}: {}", endpoint, response.readEntity(String.class));
    }
}
