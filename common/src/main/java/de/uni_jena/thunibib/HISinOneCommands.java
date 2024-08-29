package de.uni_jena.thunibib;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.xml.HISInOneServiceFlag;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.io.IOException;

import static de.uni_jena.thunibib.his.api.client.HISInOneClient.HIS_IN_ONE_BASE_URL;

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

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(mcrObjectID);
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() > 0) {
            LOGGER.warn("{} is already published. Try command 'update {0}' instead?", mcrid);
            return;
        }

        HISinOneCommandConfiguration conf = new HISinOneCommandConfiguration(mcrObject);

        try {
            String json = Utilities.transform(mcrObject, conf.getTransformerName());
            LOGGER.debug("JSON: {}", json);

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.post(conf.getPath(), json)) {
                SysValue publication = response.readEntity(conf.getResponseEntityClass());

                if (publication.getId() == 0) {
                    LOGGER.error("MCRObject {} was not published at {} with id {}", mcrid, HIS_IN_ONE_BASE_URL,
                        publication.getId());
                    return;
                }

                LOGGER.info("MCRObject {} published at {} with id {}", mcrid, HIS_IN_ONE_BASE_URL, publication.getId());

                // Update MCRObject
                mcrObject.getService().addFlag(HISInOneServiceFlag.getName(), String.valueOf(publication.getId()));
                MCRMetadataManager.update(mcrObject);
            }
        } catch (IOException | MCRAccessException e) {
            LOGGER.error("Could not publish {} to {}", mcrid, HIS_IN_ONE_BASE_URL, e);
        }
    }

    @MCRCommand(syntax = "update {0}", help = "Updates the object given by its id in HISinOne")
    public static void update(String mcrid) {
        LOGGER.info("Updating {}", mcrid);
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
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            LOGGER.warn("{} is not published. Try command 'publish {0}' first", mcrid);
            return;
        }

        HISinOneCommandConfiguration conf = new HISinOneCommandConfiguration(mcrObject);
        try {
            String hisId = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
            String json = Utilities.transform(mcrObject, conf.getTransformerName());

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.put(conf.getPath() + "/" + hisId, json)) {
                SysValue p = response.readEntity(conf.getResponseEntityClass());

                if (p.getId() == 0) {
                    LOGGER.error("MCRObject {} was not updated at {}({}) with id {}", mcrid, HIS_IN_ONE_BASE_URL,
                        conf.getPath(), p.getId());
                    return;
                }
                LOGGER.info("MCRObject {} updated at {}({}) with id {} and new lockVersion {}", mcrid,
                    HIS_IN_ONE_BASE_URL, conf.getPath(), hisId, p.getLockVersion());
            }
        } catch (IOException e) {
            LOGGER.error("Could not update {} to {}", mcrid, HIS_IN_ONE_BASE_URL, e);
        }
    }

    @MCRCommand(syntax = "delete {0}", help = "Deletes the object given by its id from HISinOne")
    public static void delete(String mcrid) {
        LOGGER.info("Deleting {}", mcrid);
    }

}
