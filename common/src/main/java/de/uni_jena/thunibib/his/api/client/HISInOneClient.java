package de.uni_jena.thunibib.his.api.client;

import jakarta.ws.rs.core.Response;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCRShutdownHandler;

import java.util.Map;

public interface HISInOneClient extends MCRShutdownHandler.Closeable, AutoCloseable {

    String HIS_IN_ONE_BASE_URL = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.BaseURL");
    String API_PATH = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.BaseURL.API.Path");

    enum AuthType {
        Basic, Bearer
    }

    default Response get(String path) {
        return get(path, false);
    }

    default Response get(String path, Map<String, String> params) {
        return get(path, params, false);
    }

    default Response get(String path, boolean omitToken) {
        return get(path, null, omitToken);
    }

    default Response post(String path, String body) {
        return post(path, body, null);
    }

    Response get(String path, Map<String, String> params, boolean omitToken);

    Response delete(String path);

    Response post(String path, String body, Map<String, String> params);

    Response put(String path, String body);
}
