package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public void setId(int id) {
        this.id = id;
    }

    public String getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(String lockVersion) {
        this.lockVersion = lockVersion;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getIso6392() {
        return iso6392;
    }

    public void setIso6392(String iso6392) {
        this.iso6392 = iso6392;
    }

    public String getIso6391() {
        return iso6391;
    }

    public void setIso6391(String iso6391) {
        this.iso6391 = iso6391;
    }

    @Override
    public String toString() {
        return id + ":" + uniqueName;
    }
}
