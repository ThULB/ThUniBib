package de.uni_jena.thunibib.matcher;

import org.jdom2.Element;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRUser;

import java.util.Map;

import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.CONNECTION_ID_NAME;
import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.LEAD_ID_NAME;

/**
 */
public class ThUniBibUserMatcherUtils {

    public static MCRUser createNewMCRUserFromModsNameElement(MCRUserMatcherDTO matcherDTO, Element modsNameElement,
        String realmID) {
        Map<String, String> nameIdentifiers = MCRUserMatcherUtils.getNameIdentifiers(modsNameElement);
        String username = matcherDTO.getMCRUser().getUserName();

        MCRUser mcrUser = new MCRUser(matcherDTO.wasMatchedOrEnriched() ? username : "artifical-" + username, realmID);
        if (matcherDTO.wasMatchedOrEnriched()) {
            ThUniBibUserMatcherUtils.addIdAttributesFromMatcherDTO(mcrUser, matcherDTO);
        }

        MCRUserMatcherUtils.enrichUserWithGivenNameIdentifiers(mcrUser, nameIdentifiers);
        mcrUser.setRealName(MCRUserMatcherUtils.getRealName(modsNameElement));
        return mcrUser;
    }

    private static void addIdAttributesFromMatcherDTO(final MCRUser mcrUser, final MCRUserMatcherDTO matcherDTO) {
        matcherDTO.getMCRUser().getAttributes()
            .stream()
            .filter(attr -> attr.getName().startsWith("id_"))
            .filter(attr -> !attr.getName().equals("id_" + LEAD_ID_NAME))
            .filter(attr -> !attr.getName().equals(CONNECTION_ID_NAME))
            .forEach(attr -> mcrUser.getAttributes().add(attr));
    }
}
