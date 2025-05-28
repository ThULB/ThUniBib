package de.uni_jena.thunibib.impex;

import de.uni_jena.thunibib.impex.importer.ConfigurableListImportJob;
import org.mycore.ubo.importer.ListImportIdprovider;

/**
 * Custom <code>ImportIdProvider</code> suitable for using with {@link ConfigurableListImportJob}.
 *
 * @author shermann (Silvio Hermann)
 */
public class DBTImportIdProvider extends ListImportIdprovider {

    @Override
    public String getImportId() {
        return super.getImportId() + "-DBT";
    }
}
