package de.uni_jena.thunibib.impex;

import org.mycore.common.MCRSessionMgr;
import org.mycore.ubo.importer.ImportIdProvider;

import java.util.Date;

public class ThUniBibCatalogImportIdProvider implements ImportIdProvider {
    @Override
    public String getImportId() {
        return DEFAULT_DATE_FORMAT.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime())) + "-SRU-PPN";
    }
}
