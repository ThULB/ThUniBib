package de.uni_jena.thunibib.publication;

import org.jdom2.Element;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRUser;

import java.util.Map;

/**
 */
public class ThUniBibUserMatcherUtils {

    public static MCRUser createNewMCRUserFromModsNameElement(MCRUserMatcherDTO matcherDTO, Element modsNameElement,
        String realmID) {
        Map<String, String> nameIdentifiers = MCRUserMatcherUtils.getNameIdentifiers(modsNameElement);
        String username = matcherDTO.getMCRUser().getUserName();
        MCRUser mcrUser = new MCRUser(username, realmID);
        MCRUserMatcherUtils.enrichUserWithGivenNameIdentifiers(mcrUser, nameIdentifiers);
        mcrUser.setRealName(MCRUserMatcherUtils.getRealName(modsNameElement));
        return mcrUser;
    }
}
