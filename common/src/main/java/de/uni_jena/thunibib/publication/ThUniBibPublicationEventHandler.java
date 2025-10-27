package de.uni_jena.thunibib.publication;

import de.uni_jena.thunibib.matcher.ThUniBibMatcherLDAP;
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
import org.mycore.ubo.matcher.MCRUserMatcher;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class ThUniBibPublicationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger();

    //TODO REMOVE
    private final static String CONFIG_MATCHERS = "MCR.user2.matching.chain";
    private final static String CONFIG_CONNECTION_STRATEGY = "MCR.user2.matching.publication.connection.strategy";
    private final static String CONFIG_DEFAULT_ROLE = "MCR.user2.IdentityManagement.UserCreation.DefaultRole";
    private final static String CONFIG_SKIP_LEAD_ID = "MCR.user2.matching.lead_id.skip";

    public final static String LEAD_ID_NAME = MCRConfiguration2.getString("MCR.user2.matching.lead_id").orElse("");
    public final static String CONNECTION_ID_NAME = "id_connection";
    public final static String LDAP_REALM = MCRConfiguration2
        .getString("MCR.user2.IdentityManagement.UserCreation.LDAP.Realm")
        .orElseThrow();

    private final static XPathExpression<Element> XPATH_GIVEN_NAME = XPATH_FACTORY.compile(
        "mods:namePart[@type='given']", Filters.element(), null, MODS_NAMESPACE);
    private final static XPathExpression<Element> XPATH_FAMILY_NAME
        = XPATH_FACTORY.compile("mods:namePart[@type='family']", Filters.element(), null, MODS_NAMESPACE);

    /** The default Role that is assigned to newly created users **/
    private String defaultRoleForNewlyCreatedUsers;

    /** Matcher to look up a matching local user **/
    private ThUniBibLocalUserMatcher localMatcher;

    private final MCRUserMatcher ldapMatcher;

    /** The configured connection strategy to "connect" publications to MCRUsers */
    private String connectionStrategy;


    public ThUniBibPublicationEventHandler() {
        super();

        this.defaultRoleForNewlyCreatedUsers = MCRConfiguration2.getString(CONFIG_DEFAULT_ROLE).orElse("submitter");
        this.localMatcher = new ThUniBibLocalUserMatcher();
        this.connectionStrategy = MCRConfiguration2.getString(CONFIG_CONNECTION_STRATEGY).orElse("");
        this.ldapMatcher = new ThUniBibMatcherLDAP();
    }

    protected void handleName(Element modsNameElement) {
        MCRUser userFromModsName = MCRUserMatcherUtils.createNewMCRUserFromModsNameElement(modsNameElement);

        MCRUserMatcherDTO ldapMatcherDTO = new MCRUserMatcherDTO(userFromModsName);
        ldapMatcherDTO = ldapMatcher.matchUser(ldapMatcherDTO);
        if (ldapMatcherDTO.wasMatchedOrEnriched()) {
            logUserMatch(modsNameElement, ldapMatcherDTO, ldapMatcher);
        }

        MCRUserMatcherDTO localMatcherDTO = localMatcher.matchUser(ldapMatcherDTO);

        if (localMatcherDTO.wasMatchedOrEnriched()) {
            handleUser(modsNameElement, localMatcherDTO.getMCRUser());
        } else if (localMatcherDTO.getMCRUser() != null && containsLeadID(modsNameElement)) {
            MCRUser newLocalUser = ThUniBibUserMatcherUtils
                .createNewMCRUserFromModsNameElement(ldapMatcherDTO, modsNameElement, LDAP_REALM);
            newLocalUser.setRealName(buildPersonNameFromMODS(modsNameElement).orElse(newLocalUser.getUserID()));
            connectModsNameElementWithMCRUser(modsNameElement, newLocalUser);
            MCRUserManager.updateUser(newLocalUser);
        } else {
            LOGGER.warn("No matching user found for for name element {}", userFromModsName.getRealName());
        }

        MCRConfiguration2.getBoolean(CONFIG_SKIP_LEAD_ID)
            .filter(Boolean::booleanValue)
            .ifPresent(trueValue -> {
                List<Element> elementsToRemove
                    = modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE)
                    .stream()
                    .filter(element -> LEAD_ID_NAME.equals(element.getAttributeValue("type")))
                    .collect(Collectors.toList());
                elementsToRemove.forEach(modsNameElement::removeContent);
            });
    }

    private boolean containsLeadID(Element modsNameElement) {
        return modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE)
            .stream().anyMatch(element -> LEAD_ID_NAME.equals(element.getAttributeValue("type")));
    }

    protected void handleUser(Element modsName, MCRUser user) {
        connectModsNameElementWithMCRUser(modsName, user);

        MCRUser storedUser = MCRUserManager.getUser(user.getUserName() + "@" + user.getRealmID());
        if (storedUser == null) {
            user.assignRole(defaultRoleForNewlyCreatedUsers);
        } else {
            for (String role : storedUser.getSystemRoleIDs().stream().toList()) {
                user.assignRole(role);
            }
        }

        MCRUserManager.updateUser(user);
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
            if (!MCRUserMatcherUtils.containsNameIdentifierWithType(modsNameElement, connectionIDType)) {
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
        Element givenName = XPATH_GIVEN_NAME.evaluateFirst(nameElement);
        Element familyName = XPATH_FAMILY_NAME.evaluateFirst(nameElement);

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
        MCRUserMatcherUtils.getNameElements(obj).forEach(modsNameElement -> handleName(modsNameElement));
        LOGGER.debug("Final document: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(obj.createXML()));
    }
}
