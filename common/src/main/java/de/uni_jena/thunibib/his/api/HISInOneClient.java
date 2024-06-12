package de.uni_jena.thunibib.his.api;

import jakarta.ws.rs.core.Response;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCRShutdownHandler;

public interface HISInOneClient extends MCRShutdownHandler.Closeable, AutoCloseable {

    String HIS_IN_ONE_BASE_URL = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.BaseURL");
    String API_PATH = "api/v1/";

    enum AuthType {
        Basic, Bearer
    }

    Response get(String path);
}
