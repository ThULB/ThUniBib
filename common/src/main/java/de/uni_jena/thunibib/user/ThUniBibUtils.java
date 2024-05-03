package de.uni_jena.thunibib.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserAttribute_;
import org.mycore.user2.MCRUser_;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ThUniBibUtils {

    protected static final Logger LOGGER = LogManager.getLogger(ThUniBibUtils.class);

    protected static final String LEAD_ID_NAME = "id_" + MCRConfiguration2.getString("MCR.user2.matching.lead_id")
        .get();

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
            MCRCategoryID categID = MCRCategoryID.fromString(classificationId + ":" + categoryId);
            MCRCategoryDAO dao = MCRCategoryDAOFactory.getInstance();
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
}
