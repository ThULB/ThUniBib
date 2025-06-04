package de.uni_jena.thunibib.his.xml;

import org.mycore.common.config.MCRConfiguration2;

public class HISInOneServiceFlag {
    static final String SERVFLAG_TYPE = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.servflag.type");

    private HISInOneServiceFlag() {
    }

    public static String getName() {
        return HISInOneServiceFlag.SERVFLAG_TYPE;
    }
}
