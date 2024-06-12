package de.uni_jena.thunibib.his.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRFunctionResolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class HISinOneResolver implements URIResolver {
    public enum URI_PART {
        language
    }

    private static Logger LOGGER = LogManager.getLogger(MCRFunctionResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Resolving '{}'", href);

        String[] parts = href.split(":");
        String entity = parts[1];
        String value = parts[2];

        switch (URI_PART.valueOf(entity)) {
            case language:
                return new JDOMSource(new Element("int").setText(String.valueOf(resolveLanguage(value))));
        }

        throw new TransformerException("Unknown entity: " + entity);
    }

    private int resolveLanguage(String rfc5646) {
        return 42;
    }
}
