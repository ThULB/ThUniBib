package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path: <code>/api/v1/cs/sys/values/publicationTypeValue</code>
 * */
public class PublicationTypeValue {
    @JsonProperty("id")
    private int id;
    @JsonProperty("lockVersion")
    private int lockVersion;
    @JsonProperty("objGuid")
    private String objGuid;
    @JsonProperty("shorttext")
    private String shortText;
    @JsonProperty("defaulttext")
    private String defaultText;
    @JsonProperty("longtext")
    private String longText;
    @JsonProperty("text")
    private String text;
    @JsonProperty("hiskeyId")
    private int hisKeyId;
    @JsonProperty("sortorder")
    private int sortOrder;
    @JsonProperty("uniquename")
    private String uniqueName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(int lockVersion) {
        this.lockVersion = lockVersion;
    }

    public String getObjGuid() {
        return objGuid;
    }

    public void setObjGuid(String objGuid) {
        this.objGuid = objGuid;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public String getLongText() {
        return longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getHisKeyId() {
        return hisKeyId;
    }

    public void setHisKeyId(int hisKeyId) {
        this.hisKeyId = hisKeyId;
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
}
