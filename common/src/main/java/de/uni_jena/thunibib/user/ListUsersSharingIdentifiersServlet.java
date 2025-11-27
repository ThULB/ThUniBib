/**
 *
 */
package de.uni_jena.thunibib.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.user2.MCRUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author shermann (Silvio Hermann)
 *
 */
public class ListUsersSharingIdentifiersServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> CONSIDERED_ID_TYPES = MCRConfiguration2
        .getOrThrow("ThUniBib.considered.shared.identifiers", MCRConfiguration2::splitValue)
        .toList();

    @Override
    protected void doGet(MCRServletJob job) throws Exception {
        Document xml = new Document();
        Element allIds = new Element("thunibib-shared-identifiers");
        xml.setRootElement(allIds);

        for (String idName : CONSIDERED_ID_TYPES) {
            allIds.addContent(buildElement(idName));
        }

        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(xml));

        /*
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(xml));
            return;
        }*/
    }

    /**
     *
     * @param idName
     * @return
     */
    private Element buildElement(String idName) {
        EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = manager.createQuery(
            "SELECT userA FROM MCRUser userA JOIN userA.attributes ua WHERE ua.name = :name AND 0 < (SELECT count(*) FROM MCRUser userB JOIN userB.attributes ub WHERE userB.internalID != userA.internalID AND ua.name = ub.name and ua.value=ub.value)");
        query.setParameter("name", idName);

        List<MCRUser> resultList = query.getResultList();
        HashMap<String, List<MCRUser>> userById = new HashMap<>();

        for (MCRUser mcrUser : resultList) {
            String value = mcrUser.getUserAttribute(idName);
            if (value == null) {
                continue;
            }

            if (userById.containsKey(value)) {
                userById.get(value).add(mcrUser);
            } else {
                ArrayList<MCRUser> list = new ArrayList<>();
                list.add(mcrUser);
                userById.put(value, list);
            }
        }

        Element idsOfType = new Element("identifier");
        idsOfType.setAttribute("type", idName);

        userById.entrySet().forEach(entry -> {
            Element idElement = new Element("identifier");
            idsOfType.addContent(idElement);
            idElement.setAttribute("value", entry.getKey());

            for (MCRUser mcrUser : entry.getValue()) {
                Element userElement = new Element("user");
                idElement.addContent(userElement);
                userElement.setAttribute("realname", mcrUser.getRealName());
                userElement.setAttribute("username", mcrUser.getUserName() + "@" + mcrUser.getRealmID());
            }
        });
        return idsOfType;
    }
}
