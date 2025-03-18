package de.uni_jena.thunibib.ldap.picker;

import org.mycore.ubo.ldap.picker.LDAPWithLocalService;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;

import javax.naming.OperationNotSupportedException;

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
        flipNameParts(results.personList);
        return results;
    }

    @Override
    protected String modifyLocalQueryWithRealm(String query) {
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
