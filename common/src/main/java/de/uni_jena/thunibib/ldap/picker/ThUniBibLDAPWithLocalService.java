package de.uni_jena.thunibib.ldap.picker;

import org.mycore.ubo.ldap.picker.LDAPWithLocalService;
import org.mycore.ubo.picker.PersonSearchResult;

import javax.naming.OperationNotSupportedException;

public class ThUniBibLDAPWithLocalService extends LDAPWithLocalService {

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult results = super.searchPerson(query);
        flipNameParts(results.personList);
        return results;
    }
}
