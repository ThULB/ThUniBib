package de.uni_jena.thunibib.his.api.v1.cs.psv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/**
 * Path: <code>/api/v1/cs/psv/person/identifier
 *
 * At least these rights: RIGHT_CS_PSV_PERSON_VIEW_PERSON_IDENTIFIER, RIGHT_CS_PSV_PERSON_VIEW_PERSON_MAINDATA
 *
 * @author shermann (Silvio Hermann)
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonIdentifier extends SysValue {

    @JsonProperty("personId")
    private int personId;

    @JsonProperty("identifierValue")
    private String identifierValue;

    @JsonProperty("identifierType")
    private String identifierType;

    public static String getValueParameterName() {
        return "identifierValue";
    }

    public static String getTypeParameterName() {
        return "typeUniquename";
    }

    public static String getPath() {
        return "cs/psv/person/identifier";
    }

    public int getPersonId() {
        return personId;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public String getIdentifierType() {
        return identifierType;
    }
}
