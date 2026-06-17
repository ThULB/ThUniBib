package de.uni_jena.thunibib.his.xml;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles resolving and creating of publishers at {@code HISinOne}.
 *
 * @author shermann (Silvio Hermann)
 * */
public class PublisherResolver extends HISinOneResolver {
    private final Map<String, SysValue> PUBLISHER_MAP = new HashMap<>();

    private static PublisherResolver INSTANCE;

    private PublisherResolver() {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the instance
     * */
    public static PublisherResolver getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new PublisherResolver();
        return INSTANCE;
    }

    public SysValue resolve(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        Map<String, String> params = new HashMap<>();
        params.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublisherWrappedValueSearch.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublisherWrappedValueSearch.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublisherWrappedValueSearch> publishers = response.readEntity(
                new GenericType<List<SysValue.PublisherWrappedValueSearch>>() {
                });

            List<SysValue.PublisherWrappedValueSearch> resultList = publishers.stream()
                .filter(pwv -> decodedValue.equals(pwv.getUniqueName()))
                .toList();

            SysValue r = !resultList.isEmpty() ? resultList.get(0) : SysValue.UnresolvedSysValue;
            if (r instanceof SysValue.PublisherWrappedValueSearch) {
                PUBLISHER_MAP.put(decodedValue, r);
            }
            return r;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Creates a new publisher. Default language is <em>German</em> and default place is <em>unknown/unbekannt</em>.
     *
     * @param displayName the name of the publisher
     *
     * @return {@link SysValue}
     * */
    public SysValue create(String displayName) {
        String decodedValue = URLDecoder.decode(displayName, StandardCharsets.UTF_8);

        SysValue.LanguageValue languageValue = (SysValue.LanguageValue) resolveLanguage("de");

        JsonObject language = new JsonObject();
        language.addProperty("id", languageValue.getId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("defaulttext", decodedValue);
        jsonObject.addProperty("uniquename", decodedValue);
        jsonObject.add("language", language);
        jsonObject.addProperty("place", "unbekannt");

        try (HISInOneClient hisClient = HISinOneClientFactory.create();

            Response response = hisClient.post(SysValue.resolve(SysValue.PublisherWrappedValueCreate.class),
                jsonObject.toString())) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublisherWrappedValueCreate.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.PublisherWrappedValueCreate publisher = response.readEntity(
                SysValue.PublisherWrappedValueCreate.class);
            return publisher;
        }
    }
}
