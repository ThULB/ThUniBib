package de.uni_jena.thunibib.his.xml;

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
import java.util.Optional;

public class PersonResolver extends HISinOneResolver {

    private final Map<String, SysValue.PersonOrganizationIdentifier> CACHE = new HashMap<>();

    private static PersonResolver instance;

    private PersonResolver() {
    }

    public static PersonResolver getInstance() {
        if (instance != null) {
            return instance;
        }

        return instance = new PersonResolver();
    }

    public SysValue resolveOrganzation(String personId) {
        String decodedValue = URLDecoder.decode(personId, StandardCharsets.UTF_8);

        if(CACHE.containsKey(decodedValue)) {
            return CACHE.get(decodedValue);
        }

        String url = SysValue.resolve(SysValue.PersonOrganizationIdentifier.class).replace("{0}", decodedValue);
        Map<String, String> param = new HashMap<>();
        param.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create(); Response response = hisClient.get(url, param)) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PersonOrganizationIdentifier.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PersonOrganizationIdentifier> orgUnits = response.readEntity(
                new GenericType<List<SysValue.PersonOrganizationIdentifier>>() {
                });

            Optional<SysValue.PersonOrganizationIdentifier> first = orgUnits.stream().findFirst();

            if (first.isPresent()) {
                CACHE.put(decodedValue, first.get());
                return first.get();
            }

            return SysValue.UnresolvedSysValue;
        } catch (Exception e) {
            LOGGER.error("Could not resolve organization unit for person {}", personId, e);
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Resolves a person by a given identifier and the type of the identifier.
     *
     * @param type the type of the identifier
     * @param value the value of the identifier
     *
     * @return {@link SysValue}
     */
    public SysValue resolvePerson(String type, String value) {
        Map<String, String> parameter = new HashMap<>();
        parameter.put(SysValue.PersonIdentifier.getTypeParameterName(), type);
        parameter.put(SysValue.PersonIdentifier.getValueParameterName(), value);
        String path = SysValue.resolve(SysValue.PersonIdentifier.class);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.post(path, null, parameter)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, path);
                return SysValue.ErroneousSysValue;
            }

            SysValue.PersonIdentifier sysValue = response.readEntity(SysValue.PersonIdentifier.class);
            return sysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }
}
