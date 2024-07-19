package de.uni_jena.thunibib.his.xml;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PeerReviewedValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationAccessTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationCreatorTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.QualificationThesisValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.ResearchAreaKdsfValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SubjectAreaValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.VisibilityValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.publisher.PublisherWrappedValue;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.DocumentType;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.GlobalIdentifierType;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Publication;
import de.uni_jena.thunibib.his.api.v1.fs.res.state.PublicationState;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
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
 * <code>hisinone:&lt;resolve|create&gt;:&lt;creatorType|documentType|publication|publicationType|globalIdentifiers|language|peerReviewed|publicationAccessType|publisher|researchAreaKdsf|subjectArea|state|thesisType|visibility&gt;:[value]</code>
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

    private static final Map<String, LanguageValue> LANGUAGE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CREATOR_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> DOCUMENT_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> IDENTIFIER_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PEER_REVIEWED_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_ACCESS_TYPE_MAP = new HashMap<>();
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
        language,
        peerReviewed,
        publication,
        publicationAccessType,
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
        String entity = parts[2];
        String fromValue;
        String hostGenre = null;

        if (ResolvableTypes.publicationType.name().equals(entity) || ResolvableTypes.documentType.name()
            .equals(entity)) {

            fromValue = parts[3];
            hostGenre = parts[4];
        } else {
            fromValue = parts[3];
        }

        var sysValue = switch (ResolvableTypes.valueOf(entity)) {
            case creatorType -> resolveCreatorType(fromValue);
            case documentType -> resolveDocumentType(fromValue, hostGenre);
            case globalIdentifiers -> resolveIdentifierType(fromValue);
            case language -> resolveLanguage(fromValue);
            case peerReviewed -> resolvePeerReviewedType(fromValue);
            case publication -> resolvePublicationLockVersion(fromValue);
            case publicationAccessType -> resolvePublicationAccessType(fromValue);
            case publicationType -> resolvePublicationType(fromValue, hostGenre);
            case publisher -> Mode.resolve.equals(mode) ? resolvePublisher(fromValue) : createPublisher(fromValue);
            case researchAreaKdsf -> resolveResearchAreaKdsf(fromValue);
            case state -> resolveState(fromValue);
            case subjectArea -> resolveSubjectArea(fromValue);
            case thesisType -> resolveThesisType(fromValue);
            case visibility -> resolveVisibility(fromValue);
        };

        LOGGER.info("Resolved {} to {}", href, sysValue);

        // TODO Make desired field part of the URI
        return new JDOMSource(new Element("int").setText(String.valueOf(
            ResolvableTypes.publication.name().equals(entity) ? sysValue.getLockVersion() : sysValue.getId())));
    }

    /**
     *
     * @param hisid the his id of a publication
     * @return
     */
    private SysValue resolvePublicationLockVersion(String hisid) {
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(Publication.getPath() + "/" + hisid)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
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

        LanguageValue languageValue = resolveLanguage("de");

        JsonObject language = new JsonObject();
        language.addProperty("id", languageValue.getId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("defaulttext", decodedValue);
        jsonObject.addProperty("uniquename", decodedValue);
        jsonObject.add("language", language);
        jsonObject.addProperty("place", "unbekannt");

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response resp = hisClient.post(PublisherWrappedValue.getPath(PublisherWrappedValue.PathType.create),
                jsonObject.toString())) {

            PublisherWrappedValue publisher = resp.readEntity(PublisherWrappedValue.class);
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
                return SysValue.ErroneousSysValue;
            }

            List<PeerReviewedValue> prTypes = response.readEntity(
                new GenericType<List<PeerReviewedValue>>() {
                });

            String text = "true".equals(peerReviewedCategId) ? "ja" : "nein";
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

            List<PublicationTypeValue> pubTypeValues = response.readEntity(
                new GenericType<List<PublicationTypeValue>>() {
                });

            String expectedType = PublicationAndDocTypeMapper.getPublicationTypeName(ubogenre, hostGenre);

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

    protected LanguageValue resolveLanguage(String rfc5646) {
        if (LANGUAGE_TYPE_MAP.containsKey(rfc5646)) {
            return LANGUAGE_TYPE_MAP.get(rfc5646);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(LanguageValue.getPath())) {

            List<LanguageValue> languageValues = response.readEntity(new GenericType<List<LanguageValue>>() {
            });

            LanguageValue languageValue = languageValues
                .stream()
                .filter(lv -> lv.getIso6391().equals(rfc5646))
                .findFirst()
                .get();

            LANGUAGE_TYPE_MAP.put(rfc5646, languageValue);
            return languageValue;
        }
    }
}
