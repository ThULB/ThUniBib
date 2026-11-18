package de.uni_jena.thunibib.impex;

import org.mycore.ubo.importer.ListImportIdprovider;

import java.util.Objects;

/**
 * Creates an import id with the current timestamp and a provided fixed postfix.
 * <br/>
 * Example:
 *
 * {@code new PostFixImportIdProvider("SCOPUS").getImportId()} results in {@code 2026-09-03 10:51:53-SCOPUS}.
 *
 * @see ListImportIdprovider
 *
 * @author shermann (Silvio Hermann)
 */
public class PostFixImportIdProvider extends ListImportIdprovider {

    private final String postfix;

    public PostFixImportIdProvider(String postfix) {
        this.postfix = Objects.requireNonNull(postfix);
    }

    @Override
    public String getImportId() {
        return super.getImportId() + "-" + postfix;
    }
}
