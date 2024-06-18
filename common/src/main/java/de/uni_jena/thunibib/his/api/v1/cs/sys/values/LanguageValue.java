package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path: <code>/api/v1/cs/sys/values/languageValue</code>
 * */
public class LanguageValue {
    @JsonProperty("id")
    int id;
    @JsonProperty("lockVersion")
    String lockVersion;
    @JsonProperty("shorttext")
    String shortText;
    @JsonProperty("text")
    String text;
    @JsonProperty("sortorder")
    int sortOrder;
    @JsonProperty("uniquename")
    String uniqueName;
    @JsonProperty("iso_639_2")
    String iso6392;
    @JsonProperty("iso_639_1")
    String iso6391;

    public int getId() {
        return id;
    }

    public String getLockVersion() {
        return lockVersion;
    }

    public String getShortText() {
        return shortText;
    }

    public String getText() {
        return text;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getIso6392() {
        return iso6392;
    }

    public String getIso6391() {
        return iso6391;
    }

    @Override
    public String toString() {
        return id + ":" + uniqueName;
    }

    static public String getPath() {
        return "cs/sys/values/languageValue";
    }
}
