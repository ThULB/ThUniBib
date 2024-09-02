package de.uni_jena.thunibib.his.xml;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.HISinOneCommands;
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PeerReviewedValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationAccessTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationCreatorTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationResourceValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.QualificationThesisValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.ResearchAreaKdsfValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SubjectAreaValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.VisibilityValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.publisher.PublisherWrappedValue;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.DocumentType;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.GlobalIdentifierType;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Journal;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Publication;
import de.uni_jena.thunibib.his.api.v1.fs.res.state.PublicationState;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration2;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * This resolver allows to resolve internal keys used by HISinOne to address its entities.
 * </p>
 *
 * Usage
 * <p>
 * <code>hisinone:&lt;resolve|create&gt;:&lt;[requested field]&gt;:&lt;creatorType|documentType|journal|publication|publicationAccessType|publicationResource|publicationType|globalIdentifiers|language|peerReviewed|publisher|researchAreaKdsf|subjectArea|state|thesisType|visibility&gt;:[value]</code>
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
    private static final String JOURNAL_TRANSFORMER = MCRConfiguration2.getStringOrThrow(
        "ThUniBib.HISinOne.Journal.Transformer.Name");

    private static final Map<String, LanguageValue> LANGUAGE_TYPE_MAP = new HashMap<>();
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
        creatorType,
        documentType,
        globalIdentifiers,
        journal,
        language,
        peerReviewed,
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
        String hostGenre = null;

        if (ResolvableTypes.publicationType.name().equals(entity) || ResolvableTypes.documentType.name()
            .equals(entity)) {
            hostGenre = parts[5];
        }

        fromValue = parts.length > 4 ? URLDecoder.decode(parts[4], StandardCharsets.UTF_8) : "";

        var sysValue = switch (ResolvableTypes.valueOf(entity)) {
            case creatorType -> resolveCreatorType(fromValue);
            case documentType -> resolveDocumentType(fromValue, hostGenre);
            case globalIdentifiers -> resolveIdentifierType(fromValue);
            case journal -> Mode.resolve.equals(mode) ? resolveJournal(fromValue) : createParent(fromValue);
            case language -> resolveLanguage(fromValue);
            case peerReviewed -> resolvePeerReviewedType(fromValue);
            case publication -> Mode.resolve.equals(mode) ? resolvePublication(fromValue) : createParent(fromValue);
            case publicationAccessType -> resolvePublicationAccessType(fromValue);
            case publicationResource -> resolvePublicationResourceType(fromValue);
            case publicationType -> resolvePublicationType(fromValue, hostGenre);
            case publisher -> Mode.resolve.equals(mode) ? resolvePublisher(fromValue) : createPublisher(fromValue);
            case researchAreaKdsf -> resolveResearchAreaKdsf(fromValue);
            case state -> resolveState(fromValue);
            case subjectArea -> resolveSubjectArea(fromValue);
            case thesisType -> resolveThesisType(fromValue);
            case visibility -> resolveVisibility(fromValue);
            default -> SysValue.UnresolvedSysValue;
        };

        LOGGER.info("Resolved {} to {}", href, String.valueOf(getFieldValue(sysValue, field)));
        return new JDOMSource(new Element("int").setText(String.valueOf(getFieldValue(sysValue, field))));
    }

    protected SysValue resolveJournal(String fromValue) {
        if (!exists(fromValue)) {
            LOGGER.warn("{} does not exist", fromValue);
            return SysValue.UnresolvedSysValue;
        }

        MCRObject host = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(fromValue));
        if (host.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisId = host.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(Journal.getPath() + "/" + hisId)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }
            Journal journal = response.readEntity(Journal.class);
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
            Response response = hisClient.get(PublicationResourceValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublicationResourceValue> availableResourceTypes = response.readEntity(
                new GenericType<List<PublicationResourceValue>>() {
                });

            // find his key of resourceType
            if (label.isPresent()) {
                String text = label.get().getText();
                Optional<PublicationResourceValue> resolved = availableResourceTypes
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
            LOGGER.warn("{} does not exist", mcrid);
            return SysValue.UnresolvedSysValue;
        }

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisid = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(Publication.getPath() + "/" + hisid)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            Publication publication = response.readEntity(Publication.class);
            return publication;
        }
    }

    private SysValue resolvePublisher(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        if (PUBLISHER_MAP.containsKey(decodedValue)) {
            return PUBLISHER_MAP.get(decodedValue);
        }

        Map<String, String> params = new HashMap<>();
        params.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(PublisherWrappedValue.getPath(), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublisherWrappedValue> publishers = response.readEntity(
                new GenericType<List<PublisherWrappedValue>>() {
                });

            List<PublisherWrappedValue> resultList = publishers.stream()
                .filter(pwv -> decodedValue.equals(pwv.getUniqueName()))
                .toList();

            SysValue r = !resultList.isEmpty() ? resultList.get(0) : SysValue.UnresolvedSysValue;
            if (r instanceof PublisherWrappedValue) {
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

        if (PUBLISHER_MAP.containsKey(decodedValue)) {
            return PUBLISHER_MAP.get(decodedValue);
        }

        LanguageValue languageValue = (LanguageValue) resolveLanguage("de");

        JsonObject language = new JsonObject();
        language.addProperty("id", languageValue.getId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("defaulttext", decodedValue);
        jsonObject.addProperty("uniquename", decodedValue);
        jsonObject.add("language", language);
        jsonObject.addProperty("place", "unbekannt");

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.post(PublisherWrappedValue.getPath(PublisherWrappedValue.PathType.create),
                jsonObject.toString())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            PublisherWrappedValue publisher = response.readEntity(PublisherWrappedValue.class);
            PUBLISHER_MAP.put(decodedValue, publisher);
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
            Response response = hisClient.get(PeerReviewedValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PeerReviewedValue> prTypes = response.readEntity(
                new GenericType<List<PeerReviewedValue>>() {
                });

            var text = switch (peerReviewedCategId) {
                case "true" -> "ja";
                case "false" -> "nein";
                default -> "keine Angabe";
            };

            Optional<PeerReviewedValue> peerReviewedValue = prTypes
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
            Response response = hisClient.get(PublicationAccessTypeValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublicationAccessTypeValue> accessTypes = response.readEntity(
                new GenericType<List<PublicationAccessTypeValue>>() {
                });

            MCRCategoryID mcrCategoryID = MCRCategoryID.fromString("accessrights:" + accessRightsCategId);
            MCRCategory mcrCategory = MCRCategoryDAOFactory.getInstance().getCategory(mcrCategoryID, 1);

            Optional<MCRLabel> label = mcrCategory.getLabel("en");

            if (!label.isPresent()) {
                return SysValue.UnresolvedSysValue;
            }

            String text = label.get().getText().toLowerCase(Locale.ROOT);
            Optional<PublicationAccessTypeValue> first = accessTypes
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
            Response response = hisClient.get(ResearchAreaKdsfValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<ResearchAreaKdsfValue> availableTypes = response.readEntity(
                new GenericType<List<ResearchAreaKdsfValue>>() {
                });

            Optional<ResearchAreaKdsfValue> raKdsfValue = availableTypes
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
            Response response = hisClient.get(GlobalIdentifierType.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<GlobalIdentifierType> availableTypes = response.readEntity(
                new GenericType<List<GlobalIdentifierType>>() {
                });

            Optional<GlobalIdentifierType> type = availableTypes
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

    protected SysValue resolvePublicationType(String ubogenre, String hostGenre) {
        if (PUBLICATION_TYPE_MAP.containsKey(ubogenre)) {
            return PUBLICATION_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(PublicationTypeValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublicationTypeValue> pubTypeValues = response.readEntity(
                new GenericType<List<PublicationTypeValue>>() {
                });

            String expectedType = PublicationAndDocTypeMapper.getPublicationTypeName(ubogenre, hostGenre);

            if (expectedType == null) {
                return SysValue.UnresolvedSysValue;
            }

            Optional<PublicationTypeValue> tpv = pubTypeValues
                .stream()
                .filter(pubType -> pubType.getUniqueName().equals(expectedType))
                .findFirst();

            PUBLICATION_TYPE_MAP.put(ubogenre, tpv.get());
            return tpv.get();
        }
    }

    /**
     * Determines the documentType on base of ubogenre/publicationType. Is currently fixed to 'Bibliographie'
     * */
    private SysValue resolveDocumentType(String ubogenre, String hostGenre) {
        if (DOCUMENT_TYPE_MAP.containsKey(ubogenre)) {
            return DOCUMENT_TYPE_MAP.get(ubogenre);
        }

        String documentTypeName = PublicationAndDocTypeMapper.getDocumentTypeName(ubogenre, hostGenre);
        if (documentTypeName == null) {
            return SysValue.UnresolvedSysValue;
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response bookResp = hisClient.get(DocumentType.getPath(DocumentType.PathType.book));
            Response articleResp = hisClient.get(DocumentType.getPath(DocumentType.PathType.article))) {

            if (bookResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(bookResp);
                return SysValue.ErroneousSysValue;
            }
            if (articleResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(articleResp);
                return SysValue.ErroneousSysValue;
            }

            List<DocumentType> ofBook = bookResp.readEntity(new GenericType<List<DocumentType>>() {
            });
            ofBook.addAll(articleResp.readEntity(new GenericType<List<DocumentType>>() {
            }));

            // remove duplicates
            List<DocumentType> list = ofBook
                .stream()
                .collect(Collectors.toMap(DocumentType::getId, existing -> existing, (existing, replace) -> existing))
                .values()
                .stream()
                .toList();

            Optional<DocumentType> documentType = list
                .stream()
                .filter(v -> v.getDefaultText().equals(documentTypeName))
                .findFirst();

            if (documentType.isPresent()) {
                DOCUMENT_TYPE_MAP.put(ubogenre, documentType.get());
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
            Response response = hisClient.get(QualificationThesisValue.getPath())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<QualificationThesisValue> thesisValues = response.readEntity(
                new GenericType<List<QualificationThesisValue>>() {
                });

            MCRCategoryID categId = MCRCategoryID.fromString("ubogenre:" + ubogenre);
            MCRCategoryID thesisCategId = MCRCategoryID.fromString("ubogenre:thesis");
            List<MCRCategory> children = MCRCategoryDAOFactory.getInstance().getChildren(thesisCategId);
            boolean isThesis = children.stream().filter(c -> c.getId().equals(categId)).findAny().isPresent();

            QualificationThesisValue sysValue = null;
            if (isThesis) {
                String text = MCRCategoryDAOFactory
                    .getInstance()
                    .getCategory(categId, -1).getLabel("de").get()
                    .getText();

                Optional<QualificationThesisValue> qtv = thesisValues.stream()
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
            Response response = hisClient.get(SubjectAreaValue.getPath())) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<SubjectAreaValue> subjectAreas = response.readEntity(
                new GenericType<List<SubjectAreaValue>>() {
                });

            Optional<SubjectAreaValue> areaValue = subjectAreas.stream()
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

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/publicationCreatorTypeValue")) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublicationCreatorTypeValue> creatorTypes = response.readEntity(
                new GenericType<List<PublicationCreatorTypeValue>>() {
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
            Response response = hisClient.get(VisibilityValue.getPath())) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<VisibilityValue> visState = response.readEntity(
                new GenericType<List<VisibilityValue>>() {
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
     * Resolves the HISinOne {@link PublicationState} by the current hsb publication status.
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
            Response response = hisClient.get(PublicationState.getPath())) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }

            List<PublicationState> pubState = response.readEntity(
                new GenericType<List<PublicationState>>() {
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
            Response response = hisClient.get(LanguageValue.getPath())) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logHISMessage(response);
                return SysValue.ErroneousSysValue;
            }
            
            List<LanguageValue> languageValues = response.readEntity(new GenericType<List<LanguageValue>>() {
            });

            Optional<LanguageValue> languageValue = languageValues
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
    protected Object getFieldValue(SysValue sysValue, String fieldName) {
        try {
            final Field field = Class.forName(SysValue.class.getName()).getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(sysValue);
            if (value == null) {
                throw new RuntimeException("Could not get value from field " + fieldName);
            }
            return value;
        } catch (Exception e) {
            LOGGER.warn("Field {} could not be obtained from SysValue {} returning id instead", fieldName, sysValue);
            return sysValue.getId();
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
            return false;
        }

        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            return false;
        }
        return true;
    }

    /**
     * Logs the response to the error log. Closes the {@link Response} object.
     *
     * @param response the response
     */
    protected final void logHISMessage(Response response) {
        LOGGER.error("HISinOne api message: {}", response.readEntity(String.class));
    }
}
