package de.uni_jena.thunibib.matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.ubo.ldap.LDAPObject;
import org.mycore.ubo.matcher.MCRUserMatcherLDAP;

import java.util.Collection;

public class ThUniBibMatcherLDAP extends MCRUserMatcherLDAP {

    private final static Logger LOGGER = LogManager.getLogger(ThUniBibMatcherLDAP.class);

    @Override
    protected String getUserNameFromLDAPUser(LDAPObject ldapUser) {
        String loginAttrName = MCRConfiguration2.getString(MCRUserMatcherLDAP.CONFIG_LDAP_LOGIN_ATTRIBUTENAME)
            .orElse("");

        if (!"eduPersonUniqueId".equals(loginAttrName) || StringUtils.isEmpty(loginAttrName)) {
            return super.getUserNameFromLDAPUser(ldapUser);
        }

        if (!ldapUser.getAttributes().containsKey(loginAttrName)) {
            throw new MCRConfigurationException("Attribute: " + loginAttrName + " (configured with: " +
                MCRUserMatcherLDAP.CONFIG_LDAP_LOGIN_ATTRIBUTENAME + ") not found in matched LDAP user, " +
                "cannot find suitable username.");
        }

        Collection<String> attributeValues = ldapUser.getAttributes().get(loginAttrName);

        String eduPersonUniqueId = attributeValues.iterator().next();
        String username = eduPersonUniqueId.substring(0, eduPersonUniqueId.indexOf("@"));

        LOGGER.info("Got username '{}' from LDAP attribute '{}'", username, loginAttrName);

        return username;
    }
}
