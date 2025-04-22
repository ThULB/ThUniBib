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
    public static SysValue publish(String mcrid) {
        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            return SysValue.UnresolvedSysValue;
        }
        return publish(MCRMetadataManager.retrieveMCRObject(id));
    }

    public static SysValue publish(MCRObject mcrObject) {
        LOGGER.info("Publishing {} to HISinOne", mcrObject.getId());

        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() > 0) {
            LOGGER.warn("{} is already published. Try command 'update {0}' instead?", mcrObject.getId());
            return SysValue.ErroneousSysValue;
        }

        HISinOneCommandConfiguration conf = new HISinOneCommandConfiguration(mcrObject);

        try {
            String json = Utilities.transform(mcrObject, conf.getTransformerName());
            LOGGER.debug("JSON: {}", json);

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.post(conf.getPath(), json)) {

                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    LOGGER.error("{}: {}", conf.getPath(), response.readEntity(String.class));
                    return SysValue.ErroneousSysValue;
                }

                SysValue publication = response.readEntity(conf.getResponseEntityClass());
                if (publication.getId() == 0) {
                    LOGGER.error("MCRObject {} was not published at {} with id {}", mcrObject.getId(),
                        HIS_IN_ONE_BASE_URL + conf.getPath(), publication.getId());
                    return SysValue.ErroneousSysValue;
                }

                LOGGER.info("MCRObject {} published at {} with id {}", mcrObject.getId(),
                    HIS_IN_ONE_BASE_URL + conf.getPath(),
                    publication.getId());

                // Update MCRObject
                mcrObject.getService().addFlag(HISInOneServiceFlag.getName(), String.valueOf(publication.getId()));
                MCRMetadataManager.update(mcrObject);

                return publication;
            }
        } catch (IOException | MCRAccessException e) {
            LOGGER.error("Could not publish {} to {}", mcrObject.getId(), HIS_IN_ONE_BASE_URL, e);
        }

        return SysValue.UnresolvedSysValue;
    }

    @MCRCommand(syntax = "update {0}", help = "Updates the object given by its id in HISinOne")
    public static SysValue update(String mcrid) {
        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            return SysValue.UnresolvedSysValue;
        }
        return update(MCRMetadataManager.retrieveMCRObject(id));
    }

    public static SysValue update(MCRObject mcrObject) {
        LOGGER.info("Updating {} to HISinOne", mcrObject);
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            LOGGER.warn("{} is not published. Try command 'publish {0}' first", mcrObject.getId());
            return SysValue.ErroneousSysValue;
        }

        HISinOneCommandConfiguration conf = new HISinOneCommandConfiguration(mcrObject);
        try {
            String hisId = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
            String json = Utilities.transform(mcrObject, conf.getTransformerName());

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.put(conf.getPath() + "/" + hisId, json)) {

                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    LOGGER.error("{}: {}", conf.getPath(), response.readEntity(String.class));
                    return SysValue.ErroneousSysValue;
                }

                SysValue p = response.readEntity(conf.getResponseEntityClass());
                if (p.getId() == 0) {
                    LOGGER.error("MCRObject {} was not updated at {} with id {}", mcrObject.getId(),
                        HIS_IN_ONE_BASE_URL + conf.getPath(), p.getId());
                    return SysValue.ErroneousSysValue;
                }
                LOGGER.info("MCRObject {} updated at {} with id {} and new lockVersion {}", mcrObject.getId(),
                    HIS_IN_ONE_BASE_URL + conf.getPath(), hisId, p.getLockVersion());
                return p;
            }
        } catch (IOException e) {
            LOGGER.error("Could not update {} to {}", mcrObject.getId(), HIS_IN_ONE_BASE_URL, e);
        }
        return SysValue.ErroneousSysValue;
    }

    @MCRCommand(syntax = "remove {0}", help = "Deletes the object given by its id from HISinOne")
    public static SysValue remove(String mcrid) {
        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            return SysValue.UnresolvedSysValue;
        }
        return remove(MCRMetadataManager.retrieveMCRObject(id));
    }

    public static SysValue remove(MCRObject mcrObject) {
        LOGGER.info("Deleting {} from HISinOne", mcrObject.getId());
        try {
            String hisId = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);

            try (HISInOneClient client = HISinOneClientFactory.create();
                Response response = client.delete(SysValue.resolve(SysValue.Publication.class) + "/" + hisId)) {

                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    LOGGER.error("{}: {}", SysValue.resolve(SysValue.Publication.class),
                        response.readEntity(String.class));
                    return SysValue.ErroneousSysValue;
                }
                Integer code = response.readEntity(Integer.class);
                if (code == 1) {
                    return SysValue.SuccessSysValue;
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Could not delete {} from {}", mcrObject.getId(), HIS_IN_ONE_BASE_URL, e);
        }
        return SysValue.ErroneousSysValue;
    }
}
