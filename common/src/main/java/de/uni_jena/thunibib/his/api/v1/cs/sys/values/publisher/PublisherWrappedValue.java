package de.uni_jena.thunibib.his.api.v1.cs.sys.values.publisher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.LanguageValue;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/**
 * Endpoint for <strong>searching</strong> publishers.
 *
 * PublisherWrappedValue
 *
 * Path: <code>/api/v1/fs/res/wrapped/publisher?q=</code>
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublisherWrappedValue extends SysValue {
    public enum PathType {
        search, create
    }

    @JsonProperty("_foreigntext")
    String _foreigntext;

    @JsonProperty("place")
    String place;

    @JsonProperty("label")
    String label;

    @JsonProperty("language")
    LanguageValue language;

    public String getForeignText() {
        return _foreigntext;
    }

    public String getPlace() {
        return place;
    }

    public String getLabel() {
        return label;
    }

    public static String getPath() {
        return getPath(PathType.search);
    }

    public static String getPath(PathType type) {
        return switch (type) {
            case create -> "fs/res/publisher";
            case search -> "fs/res/wrapped/publisher";
        };
    }
}
