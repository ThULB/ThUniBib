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

/**
 * Class handle resolving and creating of research partners at {@code HISinOne}.
 *
 * @author shermann (Silvio Hermann)
 * */
class ResearchPartnerResolver extends HISinOneResolver {
    private static ResearchPartnerResolver INSTANCE;

    private ResearchPartnerResolver() {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the instance
     * */
    public static ResearchPartnerResolver getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new ResearchPartnerResolver();
        return INSTANCE;
    }

    /**
     * Resolves the {@code id} of a research partner.
     *
     * @param namePart the {@code namePart} used for the lookup
     *
     * @return {@link SysValue}
     * */
    public SysValue resolve(String namePart) {
        String decodedValue = URLDecoder.decode(namePart, StandardCharsets.UTF_8);

        Map<String, String> params = new HashMap<>();
        params.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ResearchPartnerSearch.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ResearchPartnerSearch.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.ResearchPartnerSearch> researchPartner = response.readEntity(
                new GenericType<List<SysValue.ResearchPartnerSearch>>() {
                });

            Optional<SysValue.ResearchPartnerSearch> first = researchPartner.stream()
                .filter(rpv -> decodedValue.equals(rpv.getDefaultText()))
                .findFirst();

            return first.isPresent() ? first.get() : SysValue.UnresolvedSysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }
}
