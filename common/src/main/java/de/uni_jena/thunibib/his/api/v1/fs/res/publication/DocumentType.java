package de.uni_jena.thunibib.his.api.v1.fs.res.publication;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;

/**
 * Path: <code>fs/res/publication/documentTypes/&lt;book | article&gt;</code>
 * */
public class DocumentType extends SysValue {

    public enum PathType {
        article, book
    }

    public static String getPath(DocumentType.PathType type) {
        return switch (type) {
            case article -> "fs/res/publication/documentTypes/article";
            case book -> "fs/res/publication/documentTypes/book";
        };
    }
}
