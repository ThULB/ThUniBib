package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.HISInOneClient;
import de.uni_jena.thunibib.his.api.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.PublicationTypeValue;
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

public class HISinOneResolver implements URIResolver {

    public enum SUPPORTED_URI_PARTS {
        language, genre
    }

    private static final Logger LOGGER = LogManager.getLogger(HISinOneResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Resolving '{}'", href);

        String[] parts = href.split(":");
        String entity = parts[1];
        String value = parts[2];

        switch (SUPPORTED_URI_PARTS.valueOf(entity)) {
            case language:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveLanguage(value))));
            case genre:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveGenre(value))));
        }

        throw new TransformerException("Unknown entity: " + entity);
    }

    private int resolveGenre(String ubogenre) {
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
                    .filter(pubType -> "Sonstiger Publikationstyp".equals(pubType.getUniqueName()))
                    .findFirst().get().getId();
            } else {
                id = tpv.get().getId();
            }

            return id;
        }
    }

    private int resolveLanguage(String rfc5646) {
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
