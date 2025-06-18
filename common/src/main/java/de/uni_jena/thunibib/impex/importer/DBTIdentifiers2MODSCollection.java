package de.uni_jena.thunibib.impex.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRURIResolver;

import java.io.IOException;
import java.util.Arrays;

/**
 * Transforms dbt identifiers provided in the import-list.xed form to a modsCollections document.
 *
 * @author shermann (Silvio Hermann)
 * */
public class DBTIdentifiers2MODSCollection extends MCRContentTransformer {

    private static final Logger LOGGER = LogManager.getLogger(DBTIdentifiers2MODSCollection.class);

    protected final static String DBT_URL = MCRConfiguration2.getString(
        "MCR.MODS.EnrichmentResolver.DataSource.DBT.dbt.URI").orElseThrow();

    @Override
    public MCRJDOMContent transform(MCRContent source) throws IOException {

        Element root = new Element("modsCollection", MCRConstants.MODS_NAMESPACE);
        Document xml = new Document(root);

        Arrays.stream(source.asString().split("\\s")).filter(dbtid -> dbtid.trim().length() > 0)
            .forEach(dbtid -> {
                String uri = DBT_URL.replace("{0}", dbtid);
                try {
                    root.addContent(MCRURIResolver.obtainInstance().resolve(uri));
                } catch (Exception ex) {
                    LOGGER.error("Could not get content for dbt id uri {}", uri, ex);
                }
            });
        return new MCRJDOMContent(xml);
    }
}
