package de.uni_jena.thunibib.matcher;

import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;

import java.util.Map;
import java.util.SortedSet;

import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.CONNECTION_ID_NAME;
import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.LEAD_ID_NAME;

/**
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibUserMatcherUtils {

    final static String USERNAME_PREFIX = MCRConfiguration2
        .getStringOrThrow("ThUniBib.user2.matching.locally.created.username.prefix");

    public static MCRUser createNewMCRUserFromModsNameElement(MCRUserMatcherDTO matcherDTO, Element modsNameElement,
        String realmID) {
        Map<String, String> nameIdentifiers = MCRUserMatcherUtils.getNameIdentifiers(modsNameElement);
        String username = matcherDTO.getMCRUser().getUserName();

        MCRUser mcrUser = new MCRUser(matcherDTO.wasMatchedOrEnriched() ? username : USERNAME_PREFIX+"-" + username, realmID);
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

    public static void addNameIdentifiersToMCRUser(MCRUser user, Map<String, String> nameIdentifiers) {
        SortedSet<MCRUserAttribute> userAttributes = user.getAttributes();

        for (Map.Entry<String, String> nameIdentifier : nameIdentifiers.entrySet()) {
            String name = "id_" + nameIdentifier.getKey();
            String value = nameIdentifier.getValue();

            long count = userAttributes
                .stream()
                .filter(attr -> attr.getName().equals(name))
                .filter(attr -> attr.getValue().equals(value))
                .count();

            if (count == 0) {
                user.getAttributes().add(new MCRUserAttribute(name, value));
            }
        }
    }
}
