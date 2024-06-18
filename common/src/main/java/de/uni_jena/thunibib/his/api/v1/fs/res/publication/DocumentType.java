package de.uni_jena.thunibib.his.api.v1.fs.res.publication;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/**
 * Path: <code>fs/res/publication/documentTypes/&lt;book | article&gt;</code>
 * */
public class DocumentType extends SysValue {

    final static public String getPath() {
        return "fs/res/publication/documentTypes";
    }
}
