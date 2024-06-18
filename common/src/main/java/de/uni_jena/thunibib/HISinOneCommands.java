package de.uni_jena.thunibib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.io.IOException;

@MCRCommandGroup(name = "HISinOne Commands")
public class HISinOneCommands {
    private static final Logger LOGGER = LogManager.getLogger(HISinOneCommands.class);

    @MCRCommand(syntax = "publish {0}", help = "Publishes the object given by its id to HISinOne")
    public static void publish(String mcrid) {
        LOGGER.info("Publishing {}", mcrid);
        if (!MCRObjectID.isValid(mcrid)) {
            LOGGER.error("{} is not a valid {}", mcrid, MCRObjectID.class.getSimpleName());
            return;
        }

        MCRObjectID mcrObjectID = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(mcrObjectID)) {
            LOGGER.error("{} does not exist", mcrid);
            return;
        }

        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer("res-json-detailed");
        Document mods = MCRMetadataManager.retrieveMCRObject(mcrObjectID).createXML();
        try {
            MCRContent transformed = transformer.transform(new MCRJDOMContent(mods));
            LOGGER.info("JSON: {}", transformed.asString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @MCRCommand(syntax = "update {0}", help = "Uopdates the object given by its id in HISinOne")
    public static void update(String mcrid) {
        LOGGER.info("Updating {}", mcrid);
    }

    @MCRCommand(syntax = "delete {0}", help = "Deletes the object given by its id from HISinOne")
    public static void delete(String mcrid) {
        LOGGER.info("Deleting {}", mcrid);
    }
}
