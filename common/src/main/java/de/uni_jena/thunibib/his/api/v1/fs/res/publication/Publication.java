package de.uni_jena.thunibib.his.api.v1.fs.res.publication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/*
 * Path: <code>/api/v1/fs/res/publication</code>
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Publication extends SysValue {
    /**
     * This path is for both GET and POST requests.
     * */
    public static String getPath() {
        return "fs/res/publication";
    }
}
