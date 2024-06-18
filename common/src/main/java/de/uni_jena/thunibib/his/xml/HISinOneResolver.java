package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationCreatorTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.QualificationThesisValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SubjectAreaValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.VisibilityValue;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.DocumentType;
import de.uni_jena.thunibib.his.api.v1.fs.res.state.PublicationState;
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

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Usage:
 * <br/><br/>
 * <code>HISinOne:&lt;creatorType | genre | language | subjectArea | state | visibility&gt;:[value]</code>
 * */
public class HISinOneResolver implements URIResolver {

    private static Map<String, LanguageValue> LANGUAGE_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> CREATOR_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> DOCUMENT_TYPE_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> GENRE_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> STATE_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> SUBJECT_AREA_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> THESIS_TYPE_MAP = new HashMap<>();
    private static Map<String, SysValue> VISIBILITY_TYPE_MAP = new HashMap<>();

    public enum SUPPORTED_URI_PARTS {
        creatorType, documentType, genre, language, state, subjectArea, thesisType, visibility
    }

    private static final Logger LOGGER = LogManager.getLogger(HISinOneResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Resolving '{}'", href);

        String[] parts = href.split(":");
        String entity = parts[1];
        String value = parts[2];

        return switch (SUPPORTED_URI_PARTS.valueOf(entity)) {
            case creatorType ->
                new JDOMSource(new Element("int").setText(String.valueOf(resolveCreatorType(value).getId())));
            case documentType -> new JDOMSource(
                new Element("int").setText(String.valueOf(resolveDocumentType(value).getHisKeyId())));
            case genre -> new JDOMSource(new Element("int").setText(String.valueOf(resolveGenre(value).getId())));
            case language -> new JDOMSource(new Element("int").setText(String.valueOf(resolveLanguage(value).getId())));
            case state -> new JDOMSource(new Element("int").setText(String.valueOf(resolveState(value).getId())));
            case subjectArea ->
                new JDOMSource(new Element("int").setText(String.valueOf(resolveSubjectArea(value).getId())));
            case thesisType -> new JDOMSource(
                new Element("int").setText(String.valueOf(resolveThesisType(value).getHisKeyId())));
            case visibility ->
                new JDOMSource(new Element("int").setText(String.valueOf(resolveVisibility(value).getId())));
        };
    }

    /**
     * Determines the documentType on base of ubogenre/publicationType. Is currently fixed to 'Bibliographie'
     * */
    private SysValue resolveDocumentType(String ubogenre) {
        if (DOCUMENT_TYPE_TYPE_MAP.containsKey(ubogenre)) {
            return DOCUMENT_TYPE_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("fs/res/publication/documentTypes/book")) {

            List<DocumentType> documentTypeValues = response.readEntity(
                new GenericType<List<DocumentType>>() {
                });

            DocumentType documentType = documentTypeValues.stream()
                .filter(v -> v.getUniqueName().equals("Bibliographie"))
                .findFirst().get();

            DOCUMENT_TYPE_TYPE_MAP.put(ubogenre, documentType);
            return documentType;
        }
    }

    private SysValue resolveThesisType(String ubogenre) {
        if (THESIS_TYPE_MAP.containsKey(ubogenre)) {
            return THESIS_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/qualificationThesisValue")) {

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
            Response response = hisClient.get("cs/sys/values/subjectAreaValue")) {

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
            return null;
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

    protected SysValue resolveVisibility(String value) {
        if (VISIBILITY_TYPE_MAP.containsKey(value)) {
            return VISIBILITY_TYPE_MAP.get(value);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/visibilityValue")) {

            List<VisibilityValue> visState = response.readEntity(
                new GenericType<List<VisibilityValue>>() {
                });

            var id = switch (value) {
                case "confirmed", "unchecked" ->
                    visState.stream().filter(state -> "public".equals(state.getUniqueName())).findFirst().get();
                default -> visState.stream().filter(state -> "hidden".equals(state.getUniqueName())).findFirst()
                    .get();
            };

            VISIBILITY_TYPE_MAP.put(value, id);
            return id;
        }
    }

    protected SysValue resolveState(String value) {
        if (STATE_TYPE_MAP.containsKey(value)) {
            return STATE_TYPE_MAP.get(value);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("fs/res/state/publication")) {

            List<PublicationState> pubState = response.readEntity(
                new GenericType<List<PublicationState>>() {
                });

            var id = switch (value) {
                case "confirmed", "unchecked" ->
                    pubState.stream().filter(state -> "validiert".equals(state.getUniqueName())).findFirst().get();
                case "review" ->
                    pubState.stream().filter(state -> "Dateneingabe".equals(state.getUniqueName())).findFirst().get();
                default ->
                    pubState.stream().filter(state -> "zur Validierung".equals(state.getUniqueName())).findFirst()
                        .get();
            };

            STATE_TYPE_MAP.put(value, id);
            return id;
        }
    }

    protected SysValue resolveGenre(String ubogenre) {
        if (GENRE_TYPE_MAP.containsKey(ubogenre)) {
            return GENRE_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/publicationTypeValue")) {

            List<PublicationTypeValue> pubTypeValues = response.readEntity(
                new GenericType<List<PublicationTypeValue>>() {
                });

            Optional<PublicationTypeValue> tpv = pubTypeValues
                .stream()
                .filter(pubType -> pubType.getUniqueName().equals(MCRXMLFunctions.getDisplayName("ubogenre", ubogenre)))
                .findFirst();

            SysValue sysValue;
            if (tpv.isEmpty()) {
                sysValue = pubTypeValues.stream()
                    .filter(pubType -> "Monographie".equals(pubType.getUniqueName()))
                    .findFirst().get();
            } else {
                sysValue = tpv.get();
            }

            GENRE_TYPE_MAP.put(ubogenre, sysValue);
            return sysValue;
        }
    }

    protected LanguageValue resolveLanguage(String rfc5646) {
        if (LANGUAGE_TYPE_MAP.containsKey(rfc5646)) {
            return LANGUAGE_TYPE_MAP.get(rfc5646);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/languageValue")) {

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
