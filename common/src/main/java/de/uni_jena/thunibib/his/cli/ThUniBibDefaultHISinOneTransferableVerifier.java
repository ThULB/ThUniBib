package de.uni_jena.thunibib.his.cli;

import org.jdom2.filter.Filters;
import org.mycore.datamodel.metadata.MCRObject;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

/**
 * Default implementation of {@link HISinOneTransferableVerifier}.
 *
 * <ul>
 *  <li>{@code state} must be {@code confirmed}</li>
 *  <li>{@code partOf} must be {@code true}</li>
 *  <li>{@code researchAreaKdsf} must be set</li>
 *  <li>{@code mods:language/mods:languageTerm @type="code} must be set</li>
 * </ul>
 *
 * @see HISinOneTransferableVerifier
 *
 * @author shermann (Silvio Hermann)
 * */
public class ThUniBibDefaultHISinOneTransferableVerifier implements HISinOneTransferableVerifier {

    @Override
    public boolean isTransferable(MCRObject mcrObject) {
        Boolean b = XPATH_FACTORY.compile(
            "contains('confirmed', //servstate/@categid[1]) and //mods:mods/mods:classification[contains(@valueURI, 'partOf#true')] and //mods:mods/mods:classification[contains(@valueURI, 'researchAreaKdsf#')] and string-length(//mods:mods/mods:language/mods:languageTerm[@type='code']) > 0",
            Filters.fboolean(), null, MODS_NAMESPACE).evaluateFirst(mcrObject.createXML());
        return b;
    }
}
