package de.uni_jena.thunibib.common.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRClassTools;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Resolves arbitrary static methods of arbitrary classes. Parameters are considerd to be of type {@link java.lang.String}.
 * <br/><br/>
 * <strong>Invocation</strong><pre><code>thunibib:&lt;class name&gt;:&lt;method name&gt;:&lt;param1&gt;:&lt;param2&gt;</code></pre>
 * <br/>
 * <strong>Example</strong><pre><code>thunibib:de.uni_jena.thunibib.user.ThUniBibUtils:getLeadId:id_connection:foobar;</code></pre>
 *
 * @author shermann (Silvio Hermann)
 * */
public class ThUniBibFunctionResolver implements URIResolver {
    protected static Logger LOGGER = LogManager.getLogger(ThUniBibFunctionResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Resolving {}", href);

        String[] parts = href.split(":");
        String className = parts[1];
        String methodName = parts[2];

        int numParams = parts.length - 3;

        Object[] params = new Object[numParams];
        if (numParams > 0) {
            System.arraycopy(parts, 3, params, 0, numParams);
        }

        try {
            Class[] types = new Class[numParams];
            Arrays.fill(types, String.class);

            Object result = null;
            Method method = MCRClassTools.forName(className).getMethod(methodName, types);
            result = method.invoke(null, params);

            Element string = new Element("string");
            string.setText(result == null ? "" : String.valueOf(result));

            return new JDOMSource(string);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}
