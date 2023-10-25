package de.uni_jena.thunibib.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserAttribute_;
import org.mycore.user2.MCRUser_;

import java.util.List;
import java.util.Optional;

public class ThUniBibUtils {

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
}
