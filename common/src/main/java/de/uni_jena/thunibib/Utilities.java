package de.uni_jena.thunibib;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserAttribute_;
import org.mycore.user2.MCRUser_;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Utilities {
    protected static final Logger LOGGER = LogManager.getLogger(Utilities.class);

    protected static final String LEAD_ID_NAME = "id_" + MCRConfiguration2
        .getString("MCR.user2.matching.lead_id")
        .get();

    public static String toUpperCase(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    public static String toLowerCase(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Transforms a given {@link MCRObject} to JSON suitable for HISinOne/RES.
     *
     * @param mcrObject the object to transform
     * @param transformerName the name of the transformer to user
     *
     * @return a {@link String} as transformation result
     * */
    public static String transform(MCRObject mcrObject, String transformerName) throws IOException {
        Document mods = mcrObject.createXML();
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerName);
        MCRContent transformed = transformer.transform(new MCRJDOMContent(mods));

        return transformed.asString();
    }

    /**
     * Gets the lead for a given other name identifier for a MCRUser.
     *
     * @param nameOther the name of the id e.g. <code>connection</code>
     * @param valueOther the value of the other id e.g. <code>64cffb9e-be26-497e-8abd-dfdcf07c62cf</code>
     *
     * @return null when the lead id could not be found.
     * */
    public static String getLeadId(String nameOther, String valueOther) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MCRUser> query = cb.createQuery(MCRUser.class);
        Root<MCRUser> users = query.from(MCRUser.class);
        SetJoin<MCRUser, MCRUserAttribute> join = users.join(MCRUser_.attributes);
        List<MCRUser> resultList = em.createQuery(
                query
                    .select(users)
                    .where(cb.and(cb.equal(join.get(MCRUserAttribute_.name), nameOther), cb.equal(join.get(
                        MCRUserAttribute_.value), valueOther))))
            .getResultList();

        if (resultList.size() == 0 || resultList.size() > 1) {
            return null;
        }

        Optional<MCRUserAttribute> leadIdAttr = resultList.get(0)
            .getAttributes()
            .stream()
            .filter(attr -> attr.getName().equals(LEAD_ID_NAME))
            .findFirst();

        return leadIdAttr.isEmpty() ? null : leadIdAttr.get().getValue();
    }

    /**
     * Returns the x-mapping for the given mapping prefix.
     * <br/>
     * Example: <code>getXMapping("mir_genres", "article", "schemaOrg")</code> will return <code>ScholarlyArticle</code>
     *
     * @param classificationId
     * @param categoryId
     * @param mappingPrefix
     *
     * @return the mapping or an empty string when there is no such mapping for the given parameters
     * */
    public static String getXMapping(String classificationId, String categoryId, String mappingPrefix) {
        MCRCategory category = null;
        try {
            MCRCategoryID categID = MCRCategoryID.ofString(classificationId + ":" + categoryId);
            MCRCategoryDAO dao = MCRCategoryDAOFactory.obtainInstance();
            category = dao.getCategory(categID, 0);
            Optional<MCRLabel> label = category.getLabel("x-mapping");
            if (label.isEmpty()) {
                return "";
            }
            MCRLabel xmapping = label.get();
            if (!xmapping.getText().contains(mappingPrefix)) {
                return "";
            }

            Optional<String> result = Arrays.stream(xmapping.getText().split(" "))
                .filter(mapping -> mapping.startsWith(mappingPrefix))
                .findFirst();

            return result.isPresent() ? result.get().split(":")[1] : "";
        } catch (Throwable e) {
            LOGGER.error("Could not load {}:{}", classificationId, categoryId, e);
            return "";
        }
    }

    /**
     * Converts an unencoded URL query string to an encoded version.
     *
     * @param rawQueryString
     * @return
     */
    public static String toQueryString(String rawQueryString) {
        StringBuilder b = new StringBuilder();

        String[] nameValues = rawQueryString.split("&");
        for (int i = 0; i < nameValues.length; i++) {
            String[] split = nameValues[i].split("=");
            if (split.length == 2) {
                b.append(
                    (b.length() > 0 ? "&" : "") + split[0] + "=" + URLEncoder.encode(split[1], StandardCharsets.UTF_8));
            }
        }

        return b.toString();
    }
}
