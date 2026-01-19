package de.uni_jena.thunibib.publication;

import de.uni_jena.thunibib.matcher.ThUniBibLocalUserMatcher;
import de.uni_jena.thunibib.matcher.ThUniBibMatcherLDAP;
import de.uni_jena.thunibib.matcher.ThUniBibUserMatcherUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.ubo.matcher.MCRUserMatcher;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class ThUniBibPublicationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger();

    private final static String CONFIG_CONNECTION_STRATEGY = "MCR.user2.matching.publication.connection.strategy";
    private final static String CONFIG_DEFAULT_ROLE = "MCR.user2.IdentityManagement.UserCreation.DefaultRole";
    private final static String CONFIG_SKIP_LEAD_ID = "MCR.user2.matching.lead_id.skip";

    public final static String CONNECTION_ID_NAME = "id_connection";
    public final static String LDAP_REALM = MCRConfiguration2.getString("MCR.user2.IdentityManagement.UserCreation.LDAP.Realm").orElseThrow();
    public final static String LEAD_ID_NAME = MCRConfiguration2.getString("MCR.user2.matching.lead_id").orElseThrow();

    private final static XPathExpression<Element> XP_FAMILY_NAME = XPATH_FACTORY.compile("mods:namePart[@type='family']", Filters.element(), null, MODS_NAMESPACE);
    private final static XPathExpression<Element> XP_GIVEN_NAME = XPATH_FACTORY.compile("mods:namePart[@type='given']", Filters.element(), null, MODS_NAMESPACE);

    /** The default Role that is assigned to newly created users **/
    private final String defaultRoleForNewlyCreatedUsers;

    /** Matcher to look up a matching local user **/
    private final ThUniBibLocalUserMatcher localMatcher;

    private final MCRUserMatcher ldapMatcher;

    /** The configured connection strategy to "connect" publications to MCRUsers */
    private final String connectionStrategy;

    private final ThUniBibUserMatcherUtils matcherUtils;

    public ThUniBibPublicationEventHandler() {
        super();

        this.defaultRoleForNewlyCreatedUsers = MCRConfiguration2.getString(CONFIG_DEFAULT_ROLE).orElse("submitter");
        this.localMatcher = new ThUniBibLocalUserMatcher();
        this.connectionStrategy = MCRConfiguration2.getString(CONFIG_CONNECTION_STRATEGY).orElse("");
        this.ldapMatcher = new ThUniBibMatcherLDAP();
        this.matcherUtils = ThUniBibUserMatcherUtils.getInstance();
    }

    protected void handleName(Element modsNameElement) {
        MCRUser userFromModsName = matcherUtils.createNewMCRUserFromModsNameElement(modsNameElement);
        MCRUserMatcherDTO matcherDTO;

        MCRUser cloned = userFromModsName.clone();

        /* Only retain lead id and connection id for matching when connection or lead id present.
         * When leadid/connection id are not present retain all other id, these id are needed when the local matcher
         * trys to find users.
         */
        if(cloned.getAttributes().stream().anyMatch(attr->attr.getName().equals(CONNECTION_ID_NAME) || attr.getName().equals("id_" + LEAD_ID_NAME))) {
            cloned.setAttributes(cloned
                .getAttributes()
                .stream()
                .filter(mcrUserAttribute -> mcrUserAttribute.getName().equals("id_" + LEAD_ID_NAME) || mcrUserAttribute.getName().equals(CONNECTION_ID_NAME))
                .collect(Collectors.toCollection(TreeSet::new)));
        }

        matcherDTO = new MCRUserMatcherDTO(cloned);

        if (MCRConfiguration2.getString("MCR.user2.LDAP.ProviderURL").isPresent()) {
            matcherDTO = ldapMatcher.matchUser(matcherDTO);
        }

        if (matcherDTO.wasMatchedOrEnriched()) {
            logUserMatch(modsNameElement, matcherDTO, ldapMatcher);
        }

        MCRUserMatcherDTO localMatcherDTO = localMatcher.matchUser(matcherDTO);

        if (localMatcherDTO.wasMatchedOrEnriched()) {
            handleUser(modsNameElement, localMatcherDTO.getMCRUser());
        } else if (localMatcherDTO.getMCRUser() != null && containsLeadID(modsNameElement)) {
            handleUser(modsNameElement, matcherDTO, userFromModsName);
        } else {
            LOGGER.warn("No matching user found for for name element {}", userFromModsName.getRealName());
        }

        MCRConfiguration2.getBoolean(CONFIG_SKIP_LEAD_ID)
            .filter(Boolean::booleanValue)
            .ifPresent(trueValue -> {
                List<Element> elementsToRemove = modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE)
                    .stream()
                    .filter(element -> LEAD_ID_NAME.equals(element.getAttributeValue("type")))
                    .collect(Collectors.toList());
                elementsToRemove.forEach(modsNameElement::removeContent);
            });
    }

    /**
     * Handles locally matched (therefore existing) user.
     *
     * @param modsNameElement
     * @param user
     * */
    protected void handleUser(Element modsNameElement, MCRUser user) {
        connectModsNameElementWithMCRUser(modsNameElement, user);
        MCRUser storedUser = MCRUserManager.getUser(user.getUserName() + "@" + user.getRealmID());
        if (storedUser == null) {
            user.assignRole(defaultRoleForNewlyCreatedUsers);
        } else {
            for (String role : storedUser.getSystemRoleIDs().stream().toList()) {
                user.assignRole(role);
            }
        }

        matcherUtils.addNameIdentifiersToMCRUser(storedUser, matcherUtils.getNameIdentifiers(modsNameElement));
        if (hasUniqueNameIdentifiers(storedUser,1)) {
            MCRUserManager.updateUser(user);
        } else {
            removeDuplicateNameIdentifiers(storedUser,1);
            LOGGER.error("mods:name element {} has identifiers already present at other user", storedUser.getRealName());
        }
    }

    /**
     * Handles user matched in LDAP but not locally present.
     *
     * @param modsNameElement
     * @param matcherDTO
     * @param userFromModsName
     * */
    protected void handleUser(Element modsNameElement, MCRUserMatcherDTO matcherDTO, MCRUser userFromModsName) {
        MCRUser newLocalUser = matcherUtils.createMCRUserFromMatchedModsNameElement(matcherDTO, modsNameElement, LDAP_REALM);
        newLocalUser.setRealName(buildPersonNameFromMODS(modsNameElement).orElse(newLocalUser.getUserID()));

        // check duplicate nameIdentifiers in userdata base
        if (hasUniqueNameIdentifiers(newLocalUser, 0)) {
            connectModsNameElementWithMCRUser(modsNameElement, newLocalUser);
            MCRUserManager.updateUser(newLocalUser);
        } else {
            LOGGER.error("mods:name element {} has identifiers already present at other user", userFromModsName.getRealName());
        }
    }

    /**
     * Determines if attributes of the provided users are already present at other users. One can specify how often the
     * attribute is allowed to occur across the entire user database
     *
     * @param mcrUser the {@link MCRUser}
     * @param maxOccurrenceAllowed specify how often the attribute is allowed to occur across the entire user database
     * */
    protected boolean hasUniqueNameIdentifiers(MCRUser mcrUser, int maxOccurrenceAllowed) {
        SortedSet<MCRUserAttribute> userAttributes = mcrUser.getAttributes();

        List<MCRUserAttribute> leadIds = userAttributes
            .stream()
            .filter(attr -> attr.getName().equals("id_" + LEAD_ID_NAME))
            .toList();
        if (leadIds.size() > 1) {
            throw new DuplicatePrimaryIdException(getRuntimeExceptionMessage(leadIds, "id_" + LEAD_ID_NAME));
        }

        List<MCRUserAttribute> connIds = userAttributes
            .stream()
            .filter(attr -> attr.getName().equals(CONNECTION_ID_NAME))
            .toList();
        if (connIds.size() > 1) {
            throw new DuplicatePrimaryIdException(getRuntimeExceptionMessage(connIds, "id_" + CONNECTION_ID_NAME));
        }

        for (MCRUserAttribute attr : userAttributes) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            if (MCRUserManager.getUsers(attributeName, attributeValue).count() > maxOccurrenceAllowed) {
                return false;
            }
        }
        return true;
    }

    protected final String getRuntimeExceptionMessage(List<MCRUserAttribute> attr, String idType) {
        String i18n = "thunibib.editor.duplicate.primary.id.error";
        String affectedId = attr.stream().map(MCRUserAttribute::getValue).collect(Collectors.joining(", "));
        return MCRTranslation.translate(i18n, idType, affectedId);
    }

    /**
     * Removes attributes of the provided user when an attribute is already present at other users.
     * One can specify how often the attribute is allowed to occur across the entire user database before it is
     * considered a duplicate attribute.
     *
     * @param mcrUser the {@link MCRUser}
     * @param maxOccurrenceAllowed specify how often the attribute is allowed to occur
     * */
    protected void removeDuplicateNameIdentifiers(MCRUser mcrUser, int maxOccurrenceAllowed) {
        for (MCRUserAttribute attr : mcrUser.getAttributes()) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            if (MCRUserManager.getUsers(attributeName, attributeValue).count() > maxOccurrenceAllowed) {
                mcrUser.getAttributes().remove(attr);
            }
        }
    }

    private boolean containsLeadID(Element modsNameElement) {
        return modsNameElement
            .getChildren("nameIdentifier", MODS_NAMESPACE)
            .stream()
            .anyMatch(element -> LEAD_ID_NAME.equals(element.getAttributeValue("type")));
    }

    private void logUserMatch(Element modsNameElement, MCRUserMatcherDTO matcherDTO, MCRUserMatcher matcher) {
        MCRUser mcrUser = matcherDTO.getMCRUser();
        LOGGER.info("Found a match for user: {} of mods:name: {}, with attributes: {}, using matcher: {}",
            mcrUser.getUserName(),
            new XMLOutputter(Format.getPrettyFormat()).outputString(modsNameElement),
            mcrUser.getAttributes().stream().map(a -> a.getName() + "=" + a.getValue())
                .collect(Collectors.joining(" | ")),
            matcher.getClass());
    }

    private void connectModsNameElementWithMCRUser(Element modsNameElement, MCRUser mcrUser) {
        if ("uuid".equals(connectionStrategy)) {
            String connectionID = getOrAddConnectionID(mcrUser);
            // if not already present, persist connection in mods:name - nameIdentifier-Element
            String connectionIDType = CONNECTION_ID_NAME.replace("id_", "");
            if (!matcherUtils.containsNameIdentifierWithType(modsNameElement, connectionIDType)) {
                LOGGER.info("Connecting publication with MCRUser: {}, via nameIdentifier of type: {} " +
                    "and value: {}", mcrUser.getUserName(), connectionIDType, connectionID);
                addNameIdentifierTo(modsNameElement, connectionIDType, connectionID);
            }
        }
    }

    private String getOrAddConnectionID(MCRUser mcrUser) {
        // check if MCRUser already has a "connection" UUID
        String uuid = mcrUser.getUserAttribute(CONNECTION_ID_NAME);
        if (uuid == null) {
            // create new UUID and persist it for mcrUser
            uuid = UUID.randomUUID().toString();
            mcrUser.getAttributes().add(new MCRUserAttribute(CONNECTION_ID_NAME, uuid));
        }
        return uuid;
    }

    private void addNameIdentifierTo(Element modsName, String type, String value) {
        modsName.addContent(new Element("nameIdentifier", MODS_NAMESPACE).setAttribute("type", type).setText(value));
    }

    protected Optional<String> buildPersonNameFromMODS(Element nameElement) {
        Element givenName = XP_GIVEN_NAME.evaluateFirst(nameElement);
        Element familyName = XP_FAMILY_NAME.evaluateFirst(nameElement);

        if ((givenName != null) && (familyName != null)) {
            return Optional.of(familyName.getText() + ", " + givenName.getText());
        } else if (familyName != null) {
            return Optional.of(familyName.getText());
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        handlePublication(obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        handlePublication(obj);
    }

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        handlePublication(obj);
        MCRXMLMetadataManager.instance().update(obj.getId(), obj.createXML(), new Date());
    }

    protected void handlePublication(MCRObject obj) {
        matcherUtils.getNameElements(obj).forEach(modsNameElement -> handleName(modsNameElement));
        LOGGER.debug("Final document: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(obj.createXML()));
    }
}
