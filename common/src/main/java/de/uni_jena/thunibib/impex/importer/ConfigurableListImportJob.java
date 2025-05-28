package de.uni_jena.thunibib.impex.importer;

import org.mycore.ubo.importer.ImportIdProvider;
import org.mycore.ubo.importer.ListImportJob;

/**
 * Extends {@link ListImportJob} to allow providing {@link ImportIdProvider}.
 * */
public class ConfigurableListImportJob extends ListImportJob {

    public ConfigurableListImportJob(String type, ImportIdProvider idProvider) {
        super(type);
        super.importIdProvider = idProvider;
    }
}
