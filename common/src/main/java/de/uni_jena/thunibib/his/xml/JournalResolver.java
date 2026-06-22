package de.uni_jena.thunibib.his.xml;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import jakarta.ws.rs.core.Response;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles resolving of journals at {@code HISinOne}.
 *
 * @author shermann (Silvio Hermann)
 * */
public class JournalResolver extends HISinOneResolver {
    private static final Map<String, SysValue.Journal> JOURNAL_MAP = new HashMap<>();
    private static JournalResolver instance;

    private JournalResolver() {
    }

    public static JournalResolver getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new JournalResolver();
        return instance;
    }

    public SysValue resolve(String fromValue) {
        if (JOURNAL_MAP.containsKey(fromValue)) {
            return JOURNAL_MAP.get(fromValue);
        }

        if (!exists(fromValue)) {
            return SysValue.UnresolvedSysValue;
        }

        MCRObject host = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(fromValue));
        if (host.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisId = host.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Journal.class) + "/" + hisId)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Journal.class));
                return SysValue.ErroneousSysValue;
            }
            SysValue.Journal journal = response.readEntity(SysValue.Journal.class);

            JOURNAL_MAP.put(fromValue, journal);
            return journal;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }
}
