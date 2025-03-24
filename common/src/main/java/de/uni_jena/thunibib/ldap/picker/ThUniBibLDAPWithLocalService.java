package de.uni_jena.thunibib.ldap.picker;

import org.mycore.ubo.ldap.picker.LDAPWithLocalService;
import org.mycore.ubo.local.LocalService;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRRealmFactory;

import javax.naming.OperationNotSupportedException;
import java.util.Optional;

/**
 * Class provided basically the same functionality as {@link LDAPWithLocalService} but unifies display of person names
 * derived from different {@link IdentityService}s.
 *
 * @author shermann (Silvio Hermann)
 * */
public class ThUniBibLDAPWithLocalService extends LDAPWithLocalService {

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult results = super.searchPerson(query);

        LocalService localService = new LocalService();
        PersonSearchResult personSearchResult = localService.searchPerson(query);

        super.flipNameParts(personSearchResult.personList);
        results.join(personSearchResult, 0);

        if (LDAPWithLocalService.LDAP_REALM != null) {
            Optional.ofNullable(MCRRealmFactory.getRealm(LDAPWithLocalService.LDAP_REALM))
                .ifPresent(
                    realm -> results.join(localService.searchPerson(modifyQuery(query), realm), 0));
        }

        // flip name parts to unify display of names
        super.flipNameParts(results.personList);
        return results;
    }

    /**
     * Allow to modify the query when the {@link LocalService} is used for searching but a realm (different to
     * {@link MCRRealmFactory#getLocalRealm()}) is provided. This is useful when the name format of the stored users
     * by MyCoRe differs from that one provided by ldap.
     *
     * @param query
     * @return
     */
    protected String modifyQuery(String query) {
        String[] parts = query.split("\\s* \\s*");
        if (parts.length == 2) {
            String firstName = parts[0];
            String lastName = parts[1];

            query = lastName + ", " + firstName;
        } else if (parts.length > 2) {
            StringBuilder firstname = new StringBuilder();

            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    firstname.append(" ");
                }
                firstname.append(parts[i]);
            }
            query = firstname + ", " + parts[parts.length - 1];
        }

        return query;
    }
}
