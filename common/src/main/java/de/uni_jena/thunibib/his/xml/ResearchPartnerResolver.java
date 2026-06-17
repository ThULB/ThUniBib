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
    private final Map<String, SysValue> RESEARCH_PARTNER_MAP = new HashMap<>();

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

        if (RESEARCH_PARTNER_MAP.containsKey(decodedValue)) {
            return RESEARCH_PARTNER_MAP.get(decodedValue);
        }

        Map<String, String> params = new HashMap<>();
        params.put("q", decodedValue);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ResearchPartner.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ResearchPartner.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.ResearchPartner> researchPartner = response.readEntity(
                new GenericType<List<SysValue.ResearchPartner>>() {
                });

            Optional<SysValue.ResearchPartner> first = researchPartner.stream()
                .filter(rpv -> decodedValue.equals(rpv.getDefaultText()))
                .findFirst();

            if (first.isPresent()) {
                RESEARCH_PARTNER_MAP.put(namePart, first.get());
                return first.get();
            }

            return SysValue.UnresolvedSysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * <pre>
     *
     * {
     *   "business":{
     *     "defaulttext":"HSB TEST HERAUSGEBER",
     *     "postAddresses":[{
     *        "city":"ohne Angabe",
     *        "country":{
     *          "defaulttext":"Ohne Angabe",
     *          "id":122,
     *          "lockVersion":0,
     *          "longtext":"Ohne Angabe",
     *          "objGuid":"beccd277-407c-4ab2-8b1b-8e175d5e5344",
     *          "shorttext":"OhneAngabe",
     *          "text":"Ohne Angabe",
     *          "uniquename":"OA"
     *           }
     *        }
     *     ]
     *   }
     * }
     * </pre>
     * */
    public SysValue create(String displayName) {
        String decodedValue = URLDecoder.decode(displayName, StandardCharsets.UTF_8);
        if (RESEARCH_PARTNER_MAP.containsKey(decodedValue)) {
            return RESEARCH_PARTNER_MAP.get(decodedValue);
        }

        return SysValue.UnresolvedSysValue;
    }
}
