package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

public interface HisValue {
    int getId();

    int getLockVersion();

    String getShortText();

    String getText();

    int getSortOrder();

    String getUniqueName();
}
