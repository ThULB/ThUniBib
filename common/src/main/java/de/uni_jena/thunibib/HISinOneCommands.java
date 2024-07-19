package de.uni_jena.thunibib;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Publication;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.access.MCRAccessException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.io.IOException;

import static de.uni_jena.thunibib.his.api.client.HISInOneClient.API_PATH;
import static de.uni_jena.thunibib.his.api.client.HISInOneClient.HIS_IN_ONE_BASE_URL;

@MCRCommandGroup(name = "HISinOne Commands")
public class HISinOneCommands {
    private static final Logger LOGGER = LogManager.getLogger(HISinOneCommands.class);

    private static final String FLAG_NAME = HIS_IN_ONE_BASE_URL + API_PATH + Publication.getPath();

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

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(mcrObjectID);
        if (mcrObject.getService().getFlags(FLAG_NAME).size() > 0) {
            LOGGER.warn("{} is already published. Try command 'update {0}' instead?", mcrid);
            return;
        }

        Document mods = mcrObject.createXML();
        try {
            MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer("res-json-detailed");
            MCRContent transformed = transformer.transform(new MCRJDOMContent(mods));
            String json = transformed.asString();
            LOGGER.debug("JSON: {}", json);

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.post(Publication.getPath(), json)) {
                Publication publication = response.readEntity(Publication.class);
                LOGGER.info("MCRObject {} published at {} with id {}", mcrid, HIS_IN_ONE_BASE_URL, publication.getId());

                //Update MCRObject
                mcrObject.getService().addFlag(FLAG_NAME, String.valueOf(publication.getId()));
                MCRMetadataManager.update(mcrObject);
            }
        } catch (IOException | MCRAccessException e) {
            LOGGER.error("Could not publish {} to {}", mcrid, HIS_IN_ONE_BASE_URL, e);
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
