package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class SysValue {
    @JsonProperty("id")
    protected int id;
    @JsonProperty("lockVersion")
    private int lockVersion;
    @JsonProperty("objGuid")
    private String objGuid;
    @JsonProperty("shorttext")
    protected String shortText;
    @JsonProperty("defaulttext")
    protected String defaultText;
    @JsonProperty("longtext")
    protected String longText;
    @JsonProperty("text")
    protected String text;
    @JsonProperty("hiskeyId")
    protected int hisKeyId;
    @JsonProperty("sortorder")
    private int sortOrder;
    @JsonProperty("uniquename")
    private String uniqueName;

    public int getId() {
        return id;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public String getObjGuid() {
        return objGuid;
    }

    public String getShortText() {
        return shortText;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public String getLongText() {
        return longText;
    }

    public String getText() {
        return text;
    }

    public int getHisKeyId() {
        return hisKeyId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String toString() {
        return defaultText + ":" + id + ":lockVersion:" + lockVersion;
    }

    /**
     * This {@link SysValue} indicates an unresolved value in case a proper hisKeyId could not be obtained from HISinOne.
     * */
    @JsonIgnore
    public static final SysValue UnresolvedSysValue = new UnresolvedSysValue(-1);

    private static class UnresolvedSysValue extends SysValue {
        public UnresolvedSysValue(int id) {
            this.id = id;
        }
    }

    /**
     * This {@link SysValue} indicates an unresolved value in case an error occurred during the resolving of the key from
     * HISinOne.
     * */
    @JsonIgnore
    public static final SysValue ErroneousSysValue = new ErroneousSysValue(-2);

    private static class ErroneousSysValue extends SysValue {
        public ErroneousSysValue(int id) {
            this.id = id;
        }
    }
}
