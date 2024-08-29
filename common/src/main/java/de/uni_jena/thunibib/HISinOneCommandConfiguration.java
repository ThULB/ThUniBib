package de.uni_jena.thunibib;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Journal;
import de.uni_jena.thunibib.his.api.v1.fs.res.publication.Publication;
import org.jdom2.filter.Filters;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

/**
 * Class can be used to determine and store configuration values for publishing and updating publications in HISinOne.
 *
 * @author shermann (Silvio Hermann)
 */
class HISinOneCommandConfiguration {
    static final String PUBLICATION_TRANSFORMER = MCRConfiguration2.getStringOrThrow(
        "ThUniBib.HISinOne.Publication.Transformer.Name");

    static final String JOURNAL_TRANSFORMER = MCRConfiguration2.getStringOrThrow(
        "ThUniBib.HISinOne.Journal.Transformer.Name");

    final private boolean isJournal;

    public HISinOneCommandConfiguration(MCRObject obj) {
        isJournal = XPATH_FACTORY.compile(
                "//mods:mods/mods:genre[@type='intern'][contains('journal newspaper', substring-after(@valueURI, '#'))]",
                Filters.element(), null, MODS_NAMESPACE)
            .evaluateFirst(obj.createXML()) != null;
    }

    /**
     * Determines the name of the required transformer for building the JSON object.
     * */
    public String getTransformerName() {
        if (isJournal) {
            return JOURNAL_TRANSFORMER;
        }
        return PUBLICATION_TRANSFORMER;
    }

    /**
     * Determines the path for posting the JSON to.
     * */
    public String getPath() {
        if (isJournal) {
            return Journal.getPath();
        }
        return Publication.getPath();
    }

    /**
     * Determines the class to read the response to.
     * */
    public Class<? extends SysValue> getResponseEntityClass() {
        if (isJournal) {
            return Journal.class;
        }
        return Publication.class;
    }
}
