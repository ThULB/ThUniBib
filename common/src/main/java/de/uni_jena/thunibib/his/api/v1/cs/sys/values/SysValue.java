package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class SysValue implements HisValue {
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
        return id + ":" + uniqueName;
    }

    @JsonIgnore
    public static final SysValue EmptySysValue = new SysValue() {
        @Override
        public int getHisKeyId() {
            return -1;
        }

        @Override
        public int getId() {
            return -1;
        }
    };

    @JsonIgnore
    public static final SysValue NotObtainedSysValue = new SysValue() {
        @Override
        public int getHisKeyId() {
            return -2;
        }

        @Override
        public int getId() {
            return -2;
        }
    };
}
