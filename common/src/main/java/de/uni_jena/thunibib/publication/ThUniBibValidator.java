package de.uni_jena.thunibib.publication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;

import java.util.List;

/**
 * Validates MODS metadata of publications.
 *
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibValidator {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Matches all {@code mods:titleInfo} elements of a record. */
    private static final XPathExpression<Element> MODS_TITLES = XPathFactory
        .instance()
        .compile("//mods:mods/mods:titleInfo", Filters.element(), null, MCRConstants.MODS_NAMESPACE);

    /** Matches all {@code mods:titleInfo} elements that carry a non-empty {@code type} attribute. */
    private static final XPathExpression<Element> MODS_TITLES_WITH_TYPE = XPathFactory
        .instance()
        .compile("//mods:mods/mods:titleInfo[string-length(@type) > 0]", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);

    /**
     * Checks whether the titles of a MODS record are complete.
     * <p>
     * A record with less than two {@code mods:titleInfo} elements is always considered complete.
     * Otherwise, each title must carry an {@code xml:lang} attribute and exactly one title must be
     * untyped, i.e. all remaining titles need a {@code type} attribute.
     *
     * @param element the element to check, typically the MODS root or an ancestor of it
     * @return {@code true} if the titles are complete, {@code false} otherwise
     */
    public static boolean titlesComplete(Element element) {
        List<Element> titles = MODS_TITLES.evaluate(element);

        if (titles.size() < 2) {
            return true;
        }

        for (Element title : titles) {
            String lang = title.getAttributeValue("lang", Namespace.XML_NAMESPACE);
            if (lang == null || lang.isEmpty()) {
                LOGGER.error("No xml:lang attribute for title '{}'", new XMLOutputter().outputString(title));
                return false;
            }
        }

        int titlesCount = titles.size();
        int sizeTypedTitles = MODS_TITLES_WITH_TYPE.evaluate(element).size();

        boolean titlesComplete = (titlesCount - sizeTypedTitles) == 1;

        if (!titlesComplete) {
            LOGGER.error("{} title/s is/are missing a type attribute", (titlesCount - 1 - sizeTypedTitles));
        }

        return titlesComplete;
    }
}
