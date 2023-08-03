package de.uni_jena.thunibib;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

import org.mycore.common.MCRMailer;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;

public class ThUniBibMailer {
    private static final Logger LOGGER = LogManager.getLogger(ThUniBibMailer.class);

    static final String PREFIX = "UBO.Scopus.Importer.Mail.";
    static final String MAIL_TO = MCRConfiguration2.getString(PREFIX + "To").get();
    static final String MAIL_PARAM = MCRConfiguration2.getString(PREFIX + "Param").get();
    static final String MAIL_XSL = MCRConfiguration2.getString(PREFIX + "XSL").get();

    public static void sendMail(String importId, List<MCRObject> objects, String status, String source)
        throws Exception {

        if (!(objects.size() > 0 && MAIL_XSL != null)) {
            return;
        }

        LOGGER.info("Sending Mail for import id {}", importId);
        Element root = new Element(status);
        root.setAttribute("source", source);

        for (MCRObject obj : objects) {
            Element xml = obj.createXML().detachRootElement();
            root.addContent(xml);
        }

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(MAIL_PARAM, MAIL_TO);

        MCRMailer.sendMail(new Document(root.detach()), MAIL_XSL, parameters);
    }
}
