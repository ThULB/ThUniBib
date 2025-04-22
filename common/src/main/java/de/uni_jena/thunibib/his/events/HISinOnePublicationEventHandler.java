package de.uni_jena.thunibib.his.events;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.cli.HISinOneCommands;
import de.uni_jena.thunibib.his.xml.HISInOneServiceFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

import java.util.ArrayList;

/**
 * Handles the publishing of publications to HISinOne.
 *
 * @author shermann (Silvio Hermann)
 */
public class HISinOnePublicationEventHandler extends MCREventHandlerBase {
    private static final Logger LOGGER = LogManager.getLogger(HISinOnePublicationEventHandler.class);

    /**
     *
     * @param evt
     * @param obj
     */
    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        ArrayList<String> flags = obj.getService().getFlags("status");
        if (flags == null || flags.isEmpty()) {
            return;
        }

        if (isPublished(obj) && !hasHISinOneFlag(obj)) {
            SysValue sysValue = HISinOneCommands.publish(obj);
            LOGGER.info("{}", sysValue);
        }
    }

    /**
     *
     * @param evt
     * @param obj
     */
    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if (!isPublished(obj)) {
            return;
        }

        if (!hasHISinOneFlag(obj)) {
            handleObjectCreated(evt, obj);
            return;
        }

        if (!onlyHISFlagAdded((MCRObject) evt.get(MCREvent.OBJECT_OLD_KEY), obj)) {
            SysValue sysValue = HISinOneCommands.update(obj);
            LOGGER.info("{}", sysValue);
        }
    }

    /**
     *
     * @param evt
     * @param obj
     */
    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        if (!isPublished(obj) || !hasHISinOneFlag(obj)) {
            return;
        }
        SysValue sysValue = HISinOneCommands.remove(obj);
        LOGGER.info("{}", sysValue);
    }

    protected boolean isPublished(MCRObject obj) {
        String status = obj.getService().getFlags("status").get(0);
        return ("confirmed".equals(status));
    }

    protected boolean hasHISinOneFlag(MCRObject obj) {
        ArrayList<String> flags = obj.getService().getFlags(HISInOneServiceFlag.getName());
        return flags != null && !flags.isEmpty();
    }

    private boolean onlyHISFlagAdded(MCRObject old, MCRObject actual) {
        int newFlagsSize = actual.getService().getFlags(HISInOneServiceFlag.getName()).size();
        int oldFlagsSize = old.getService().getFlags(HISInOneServiceFlag.getName()).size();

        return oldFlagsSize == 0 && newFlagsSize > 0;
    }
}
