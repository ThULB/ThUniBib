package de.uni_jena.thunibib;

import org.jdom2.Document;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRObject;

import java.io.IOException;
import java.util.Locale;

public class Utilities {
    public static String toUpperCase(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    public static String toLowerCase(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Transforms a given {@link MCRObject} to JSON suitable for HISinOne/RES.
     *
     * @param mcrObject the object to transform
     * @param transformerName the name of the transformer to user
     *
     * @return a {@link String} as transformation result
     * */
    public static String transform(MCRObject mcrObject, String transformerName) throws IOException {
        Document mods = mcrObject.createXML();
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerName);
        MCRContent transformed = transformer.transform(new MCRJDOMContent(mods));

        return transformed.asString();
    }
}
