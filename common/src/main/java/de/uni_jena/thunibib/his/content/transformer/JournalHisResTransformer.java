package de.uni_jena.thunibib.his.content.transformer;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.xml.HISInOneServiceFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRContent;

import java.io.IOException;

import static de.uni_jena.thunibib.his.api.client.HISInOneClient.HIS_IN_ONE_BASE_URL;

/**
 * Class creates JSON reflecting Journals in HISinOne.
 *
 * @author shermann (Silvio Hermann)
 * */
public class JournalHisResTransformer extends PublicationHisResTransformer {
    private static final Logger LOGGER = LogManager.getLogger(JournalHisResTransformer.class);

    /**
     * Converts a mycoreobject of genre type journal to json for HISinOne.
     *
     * @param source
     * @return
     * @throws IOException
     */
    @Override
    protected JsonObject toJSON(MCRContent source) throws IOException {
        try {
            Document xml = source.asXML();
            JsonObject jsonObject = new JsonObject();

            addPropertyInt(jsonObject, "//servflag[@type='" + HISInOneServiceFlag.getName() + "-lockVersion']", xml, "lockVersion");
            addPropertyInt(jsonObject, "//servflag[@type='" + HISInOneServiceFlag.getName() + "']", xml, "id");

            addProperty(jsonObject, "//mods:mods/mods:titleInfo/mods:title[1]", xml, "defaulttext", true);
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:language/mods:languageTerm[@authorityURI='" + HIS_IN_ONE_BASE_URL + "'][1]", xml, "language");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'state/publication')]", xml, "status");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'publisher')]", xml,"publisher");
            addGlobalIdentifiers(jsonObject,xml,"//mods:identifier[contains('issn url', @type)][contains(@typeURI, '" + HIS_IN_ONE_BASE_URL + "')]");

            return jsonObject;
        } catch (JDOMException e) {
            throw new IOException(
                "Could not generate JSON from " + source.getClass().getSimpleName() + ": " + source.getSystemId(), e);
        }
    }
}
