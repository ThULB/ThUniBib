package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Publication;

import static de.uni_jena.thunibib.his.api.client.HISInOneClient.HIS_IN_ONE_BASE_URL;

public class HISInOneServiceFlag {
    private HISInOneServiceFlag() {
    }

    public static String getName() {
        return "his-id";
    }
}
