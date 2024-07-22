package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path: <code>/api/v1/cs/sys/values/languageValue</code>
 * */
public class LanguageValue extends SysValue {

    @JsonProperty("iso_639_2")
    String iso6392;
    @JsonProperty("iso_639_1")
    String iso6391;

    public String getIso6392() {
        return iso6392;
    }

    public String getIso6391() {
        return iso6391;
    }

    static public String getPath() {
        return "cs/sys/values/languageValue";
    }
}
