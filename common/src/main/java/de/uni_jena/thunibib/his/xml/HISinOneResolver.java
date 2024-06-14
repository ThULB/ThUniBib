package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationCreatorTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SubjectAreaValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.VisibilityValue;
import de.uni_jena.thunibib.his.api.v1.fs.res.state.PublicationState;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRXMLFunctions;

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
 * <code>HISinOne:[creatorType | genre | language | subjectArea | state | visibility]:[value]</code>
 * */
public class HISinOneResolver implements URIResolver {

    private static Map<String, Integer> CREATOR_TYPE_MAP = new HashMap<>();
    private static Map<String, Integer> GENRE_TYPE_MAP = new HashMap<>();
    private static Map<String, Integer> LANGUAGE_TYPE_MAP = new HashMap<>();
    private static Map<String, Integer> STATE_TYPE_MAP = new HashMap<>();
    private static Map<String, Integer> SUBJECT_AREA_TYPE_MAP = new HashMap<>();
    private static Map<String, Integer> VISIBILITY_TYPE_MAP = new HashMap<>();

    public enum SUPPORTED_URI_PARTS {
        creatorType, genre, language, state, subjectArea, visibility
    }

    private static final Logger LOGGER = LogManager.getLogger(HISinOneResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Resolving '{}'", href);

        String[] parts = href.split(":");
        String entity = parts[1];
        String value = parts[2];

        switch (SUPPORTED_URI_PARTS.valueOf(entity)) {
            case creatorType:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveCreatorType(value))));
            case genre:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveGenre(value))));
            case language:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveLanguage(value))));
            case state:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveState(value))));
            case subjectArea:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveSubjectArea(value))));
            case visibility:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveVisibility(value))));
            default:
                throw new TransformerException("Unknown entity: " + entity);
        }
    }

    private int resolveSubjectArea(String value) {
        if (SUBJECT_AREA_TYPE_MAP.containsKey(value)) {
            return SUBJECT_AREA_TYPE_MAP.get(value);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/subjectAreaValue")) {

            List<SubjectAreaValue> subjectAreas = response.readEntity(
                new GenericType<List<SubjectAreaValue>>() {
                });

            Optional<SubjectAreaValue> areaValue = subjectAreas.stream()
                .filter(subjectAreaValue -> value.equals(subjectAreaValue.getUniqueName()))
                .findFirst();

            if (areaValue.isPresent()) {
                SUBJECT_AREA_TYPE_MAP.put(value, areaValue.get().getId());
                return areaValue.get().getId();
            }
            return -1;
        }
    }

    protected int resolveCreatorType(String value) {
        if (CREATOR_TYPE_MAP.containsKey(value)) {
            return CREATOR_TYPE_MAP.get(value);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/publicationCreatorTypeValue")) {

            List<PublicationCreatorTypeValue> creatorTypes = response.readEntity(
                new GenericType<List<PublicationCreatorTypeValue>>() {
                });

            var id = switch (value) {
                default -> creatorTypes.stream().filter(state -> "Autor/-in" .equals(state.getUniqueName())).findFirst()
                    .get().getId();
            };

            CREATOR_TYPE_MAP.put(value, id);
            return id;
        }
    }

    protected int resolveVisibility(String value) {
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
                    visState.stream().filter(state -> "public" .equals(state.getUniqueName())).findFirst().get()
                        .getId();
                default -> visState.stream().filter(state -> "hidden" .equals(state.getUniqueName())).findFirst()
                    .get().getId();
            };

            VISIBILITY_TYPE_MAP.put(value, id);
            return id;
        }
    }

    protected int resolveState(String value) {
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
                    pubState.stream().filter(state -> "validiert" .equals(state.getUniqueName())).findFirst().get()
                        .getId();
                case "review" ->
                    pubState.stream().filter(state -> "Dateneingabe" .equals(state.getUniqueName())).findFirst().get()
                        .getId();
                default ->
                    pubState.stream().filter(state -> "zur Validierung" .equals(state.getUniqueName())).findFirst()
                        .get().getId();
            };

            STATE_TYPE_MAP.put(value, id);
            return id;
        }
    }

    protected int resolveGenre(String ubogenre) {
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

            int id;
            if (tpv.isEmpty()) {
                id = pubTypeValues.stream()
                    .filter(pubType -> "Sonstiger Publikationstyp" .equals(pubType.getUniqueName()))
                    .findFirst().get().getId();
            } else {
                id = tpv.get().getId();
            }

            GENRE_TYPE_MAP.put(ubogenre, id);
            return id;
        }
    }

    protected int resolveLanguage(String rfc5646) {
        if (LANGUAGE_TYPE_MAP.containsKey(rfc5646)) {
            return LANGUAGE_TYPE_MAP.get(rfc5646);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/languageValue")) {

            List<LanguageValue> languageValues = response.readEntity(new GenericType<List<LanguageValue>>() {
            });

            int id = languageValues
                .stream()
                .filter(lv -> lv.getIso6391().equals(rfc5646))
                .findFirst()
                .get()
                .getId();
            LANGUAGE_TYPE_MAP.put(rfc5646, id);
            return id;
        }
    }
}
