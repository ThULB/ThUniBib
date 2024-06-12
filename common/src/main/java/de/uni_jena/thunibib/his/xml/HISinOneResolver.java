package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.HISInOneClient;
import de.uni_jena.thunibib.his.api.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.util.List;

public class HISinOneResolver implements URIResolver {

    public enum SUPPORTED_URI_PARTS {
        language
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
        }

        throw new TransformerException("Unknown entity: " + entity);
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
