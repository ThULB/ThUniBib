package de.uni_jena.thunibib.publication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.ubo.matcher.MCRUserMatcher;
import org.mycore.ubo.matcher.MCRUserMatcherDTO;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.CONNECTION_ID_NAME;
import static de.uni_jena.thunibib.publication.ThUniBibPublicationEventHandler.LEAD_ID_NAME;

/**
 * Given a MCRUser, match against the local MCRUsers, returning the given User or an existing local one if matched.
 * If matched, the returned local MCRUsers attributes are enriched by attributes from the given MCRUser.
 * The attribute id_connection is considered unique and can not be enriched/added a second time.
 *
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibLocalUserMatcher implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger();

    @Override
    public MCRUserMatcherDTO matchUser(MCRUserMatcherDTO matcherDTO) {
        List<MCRUser> matchingUsers = new ArrayList<>();
        MCRUser providedUser = matcherDTO.getMCRUser();

        // check for matching lead id
        matchingUsers.addAll(getUsersByAttributeValue(LEAD_ID_NAME, providedUser.getUserAttribute(LEAD_ID_NAME)));

        // check for matching connection id
        if (matchingUsers.isEmpty()) {
            matchingUsers.addAll(
                getUsersByAttributeValue(CONNECTION_ID_NAME, providedUser.getUserAttribute(CONNECTION_ID_NAME)));
        }

        // check for any other id
        if (matchingUsers.isEmpty()) {
            matchingUsers.addAll(getUsersForGivenAttributes(providedUser.getAttributes()));

            // remove all matching users where lead id differs from mcrUser lead id
            matchingUsers = matchingUsers
                .stream()
                .filter(u ->
                    u.getUserAttribute(LEAD_ID_NAME) != null && providedUser.getUserAttribute(LEAD_ID_NAME) != null
                        && !(u.getUserAttribute(LEAD_ID_NAME).equals(providedUser.getUserAttribute(LEAD_ID_NAME))))
                .collect(Collectors.toList());
        }

        switch (matchingUsers.size()) {
            case 0:
                matcherDTO.setMatchedOrEnriched(false);
                return matcherDTO;
            case 1: {
                MCRUser matchingUser = matchingUsers.get(0);

                LOGGER.info(
                    "Found local matching user! Matched user: {} and attributes: {} with local user: {} and attributes: {}",
                    providedUser.getUserName(),
                    providedUser.getAttributes()
                        .stream()
                        .map(a -> a.getName() + "=" + a.getValue())
                        .collect(Collectors.joining(" | ")),
                    matchingUser.getUserName(),
                    matchingUser.getAttributes()
                        .stream()
                        .map(a -> a.getName() + "=" + a.getValue())
                        .collect(Collectors.joining(" | ")));

                boolean hasMatchingUserConnectionKey = matchingUser.getUserAttribute(CONNECTION_ID_NAME) != null;

                // only add attributes which are not present, don't add duplicate connection attributes
                List<MCRUserAttribute> providedAttributes = providedUser
                    .getAttributes()
                    .stream()
                    .filter(attribute -> !isOtherUsersAttribute(attribute))
                    .filter(attribute -> !attribute.getName().equals(CONNECTION_ID_NAME)
                        || !hasMatchingUserConnectionKey)
                    .filter(Predicate.not(matchingUser.getAttributes()::contains))
                    .toList();

                matchingUser
                    .getAttributes()
                    .addAll(providedAttributes);

                matcherDTO.setMCRUser(matchingUser);
                matcherDTO.setMatchedOrEnriched(true);
                return matcherDTO;
            }
            default: {
                LOGGER.error("Found more than one matching users for the given attributes: {}",
                    matchingUsers.stream().map(MCRUser::getUserID).collect(
                        Collectors.joining(", ")));

                matcherDTO.setMCRUser(null);
                matcherDTO.setMatchedOrEnriched(false);
                return matcherDTO;
            }
        }
    }

    private boolean isOtherUsersAttribute(MCRUserAttribute mcrAttribute) {
        String attributeName = mcrAttribute.getName();
        String attributeValue = mcrAttribute.getValue();
        List<MCRUser> mcrUsers = MCRUserManager.getUsers(attributeName, attributeValue).toList();

        return mcrUsers.size() > 0;
    }

    protected Set<MCRUser> getUsersForGivenAttributes(SortedSet<MCRUserAttribute> mcrAttributes) {
        Set<MCRUser> users = new HashSet<>();
        for (MCRUserAttribute mcrAttribute : mcrAttributes) {
            String attributeName = mcrAttribute.getName();
            String attributeValue = mcrAttribute.getValue();
            users.addAll(MCRUserManager.getUsers(attributeName, attributeValue).toList());
        }
        return users;
    }

    protected List<MCRUser> getUsersByAttributeValue(String name, String value) {
        if (value == null || name == null) {
            return new ArrayList<>();
        }
        return MCRUserManager.getUsers(name, value).toList();
    }
}
