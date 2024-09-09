package de.uni_jena.thunibib.his.api.v1.cs.psv;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/**
 * Path: <code>/api/v1/cs/psv/person/identifier
 *
 * At least one of these rights: RIGHT_CS_PSV_PERSON_VIEW_PERSON_IDENTIFIER
 * */
public class PersonIdentifier extends SysValue {

    public static String getValueParamaterName() {
        return "identifierValue";
    }

    public static String getTypeParamaterName() {
        return "typeUniquename";
    }

    public static String getPath() {
        return "cs/psv/person/identifier";
    }
}
