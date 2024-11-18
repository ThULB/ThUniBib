package de.uni_jena.thunibib.common.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRObject;

import java.util.List;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

/**
 * This event handler removes previously inserted <code>mods:nameIdentifier[@type='connection']</code> element from a
 * <code>mods:name</code> element when that element has a <code>mods:affiliation[@valueURI="isAffiliated#false"</code>
 * element.
 *
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibAffiliationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(ThUniBibAffiliationEventHandler.class);

    protected static final XPathExpression<Element> XPATH = XPATH_FACTORY.compile(
        "//mods:name[mods:affiliation[contains(@valueURI, 'isAffiliated#false')]]/mods:nameIdentifier[@type='connection']",
        Filters.element(), null, MODS_NAMESPACE);

    protected void processAffiliation(MCREvent evt, MCRObject obj) {
        Document xml = obj.createXML();
        List<Element> connectionIDs = XPATH.evaluate(xml);

        if (connectionIDs.isEmpty()) {
            return;
        }

        // remove mods:nameIdentifier[@type = 'connection']
        for (Element connectionID : connectionIDs) {
            LOGGER.info("Removing mods:nameIdentifier[type='connection'][text()='{}'] of mods:name element in {}",
                connectionID.getText(), obj.getId());
            connectionID.detach();
        }

        Element updatedMODSContainer = XPATH_FACTORY
            .compile("//def.modsContainer", Filters.element(), null, MODS_NAMESPACE)
            .evaluateFirst(xml);

        // update metadata element
        if (updatedMODSContainer != null) {
            MCRMetaElement e = new MCRMetaElement();
            e.setFromDOM(updatedMODSContainer);
            obj.getMetadata().setMetadataElement(e);
        }
    }

    /**
     *
     * @param evt
     * @param obj
     */
    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        this.processAffiliation(evt, obj);
    }

    /**
     *
     * @param evt
     * @param obj
     */
    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        this.processAffiliation(evt, obj);
    }
}
