package de.uni_jena.thunibib.matcher;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.CONNECTION_ID_NAME;
import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.LEAD_ID_NAME;
import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

/**
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibUserMatcherUtils {

    final static String USERNAME_PREFIX = MCRConfiguration2
        .getStringOrThrow("ThUniBib.user2.matching.locally.created.username.prefix");

    private static ThUniBibUserMatcherUtils instance;

    private ThUniBibUserMatcherUtils() {

    }

    /**
     * Obtain the singleton instance of this class.
     *
     * @return the singleton instance of this class
     * */
    public static ThUniBibUserMatcherUtils getInstance() {
        if (instance == null) {
            instance = new ThUniBibUserMatcherUtils();
        }
        return instance;
    }

    public MCRUser createMCRUserFromMatchedModsNameElement(MCRUserMatcherDTO matcherDTO, Element modsNameElement,
        String realmID) {
        Map<String, List<String>> nameIdentifiers = getNameIdentifiers(modsNameElement);
        String username = matcherDTO.getMCRUser().getUserName();

        MCRUser mcrUser = new MCRUser(matcherDTO.wasMatchedOrEnriched() ? username : USERNAME_PREFIX+"-" + username, realmID);
        if (matcherDTO.wasMatchedOrEnriched()) {
            addIdAttributesFromMatcherDTO(mcrUser, matcherDTO);
            if(matcherDTO.getMCRUser().getEMailAddress() != null) {
                mcrUser.setEMail(matcherDTO.getMCRUser().getEMailAddress());
            }
        }

        addNameIdentifiersToMCRUser(mcrUser, nameIdentifiers);
        mcrUser.setRealName(getRealName(modsNameElement));
        return mcrUser;
    }

    public MCRUser createNewMCRUserFromModsNameElement(Element modsNameElement) {
        return createNewMCRUserFromModsNameElement(modsNameElement, MCRRealmFactory.getLocalRealm().getID());
    }

    public MCRUser createNewMCRUserFromModsNameElement(Element modsNameElement, String realmID) {
        String userName = UUID.randomUUID().toString();
        Map<String, List<String>> nameIdentifiers = getNameIdentifiers(modsNameElement);

        MCRUser mcrUser = new MCRUser(userName, realmID);
        addNameIdentifiersToMCRUser(mcrUser, nameIdentifiers);
        mcrUser.setRealName(getRealName(modsNameElement));
        return mcrUser;
    }

    protected String getRealName(Element modsNameElement) {
        Element givenName = (Element) XPATH_FACTORY.compile("mods:namePart[@type='given']",
            Filters.element(), (Map) null, new Namespace[] { MODS_NAMESPACE }).evaluateFirst(modsNameElement);
        Element familyName = (Element) XPATH_FACTORY.compile("mods:namePart[@type='family']",
            Filters.element(), (Map) null, new Namespace[] { MODS_NAMESPACE }).evaluateFirst(modsNameElement);
        if (givenName != null && familyName != null) {
            String var10000 = familyName.getText();
            return var10000 + ", " + givenName.getText();
        } else {
            return familyName != null ? familyName.getText() : null;
        }
    }

    private void addIdAttributesFromMatcherDTO(final MCRUser mcrUser, final MCRUserMatcherDTO matcherDTO) {
        matcherDTO.getMCRUser().getAttributes()
            .stream()
            .filter(attr -> attr.getName().startsWith("id_"))
            .filter(attr -> !attr.getName().equals("id_" + LEAD_ID_NAME))
            .filter(attr -> !attr.getName().equals(CONNECTION_ID_NAME))
            .forEach(attr -> mcrUser.getAttributes().add(attr));
    }

    public void addNameIdentifiersToMCRUser(MCRUser user, Map<String, List<String>> nameIdentifiers) {
        SortedSet<MCRUserAttribute> userAttributes = user.getAttributes();
        nameIdentifiers.forEach((type, values) -> {
            String name = "id_" + type;

            for (String value : values) {
                long count = userAttributes
                    .stream()
                    .filter(attr -> attr.getName().equals(name))
                    .filter(attr -> attr.getValue().equals(value))
                    .count();
                if (count == 0) {
                    user.getAttributes().add(new MCRUserAttribute(name, value));
                }
            }
        });
    }

    public List<Element> getNameElements(MCRObject obj) {
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        return wrapper.getElements("mods:name[@type='personal']");
    }

    public boolean containsNameIdentifierWithType(Element modsNameElement, String identifierType) {
        return getNameIdentifiers(modsNameElement).containsKey(identifierType);
    }

    /**
     * Supports multiple name identifiers with same type.
     *
     * */
    public Map<String, List<String>> getNameIdentifiers(Element modsNameElement) {
        Map<String, List<String>> nameIdentifiers = new HashMap();

        for (Element identifierElement : modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE)) {
            String type = identifierElement.getAttributeValue("type");

            if (type == null) {
                continue;
            }

            String value = identifierElement.getText();
            List<String> lType = nameIdentifiers.get(type);

            if (lType == null) {
                lType = new ArrayList<>();
                nameIdentifiers.put(type, lType);
            }
            lType.add(value);
        }

        return nameIdentifiers;
    }
}
