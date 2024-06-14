package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationCreatorTypeValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
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
import java.util.List;
import java.util.Optional;

/**
 * Usage:
 * <br/><br/>
 * <code>HISinOne:[creatorType | genre | language | state | visibility]:[value]</code>
 * */
public class HISinOneResolver implements URIResolver {

    public enum SUPPORTED_URI_PARTS {
        creatorType, genre, language, state, visibility
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
            case visibility:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveVisibility(value))));
            default:
                throw new TransformerException("Unknown entity: " + entity);
        }
    }

    protected int resolveCreatorType(String value) {
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/publicationCreatorTypeValue")) {

            List<PublicationCreatorTypeValue> creatorTypes = response.readEntity(
                new GenericType<List<PublicationCreatorTypeValue>>() {
                });

            return switch (value) {
                default -> creatorTypes.stream().filter(state -> "Autor/-in" .equals(state.getUniqueName())).findFirst()
                    .get().getId();
            };
        }
    }

    protected int resolveVisibility(String value) {
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("cs/sys/values/visibilityValue")) {

            List<VisibilityValue> visState = response.readEntity(
                new GenericType<List<VisibilityValue>>() {
                });

            return switch (value) {
                case "confirmed", "unchecked" ->
                    visState.stream().filter(state -> "public" .equals(state.getUniqueName())).findFirst().get()
                        .getId();
                default -> visState.stream().filter(state -> "hidden" .equals(state.getUniqueName())).findFirst()
                    .get().getId();
            };
        }
    }

    protected int resolveState(String value) {
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get("fs/res/state/publication")) {

            List<PublicationState> pubState = response.readEntity(
                new GenericType<List<PublicationState>>() {
                });

            return switch (value) {
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
        }
    }

    protected int resolveGenre(String ubogenre) {
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

            return id;
        }
    }

    protected int resolveLanguage(String rfc5646) {
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
            return id;
        }
    }
}
