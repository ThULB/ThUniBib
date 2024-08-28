package de.uni_jena.thunibib.his.api.v1.fs.res.publication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/*
 * Path: <code>/api/v1/fs/res/journal</code>
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Journal extends SysValue {
    /**
     * This path is for both GET and POST requests.
     * */
    public static String getPath() {
        return "fs/res/journal";
    }
}
