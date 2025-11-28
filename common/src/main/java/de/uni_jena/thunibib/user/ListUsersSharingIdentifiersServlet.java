/**
 *
 */
package de.uni_jena.thunibib.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;

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
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Document xml = new Document();
        Element allIds = new Element("thunibib-shared-identifiers");
        xml.setRootElement(allIds);

        for (String idName : CONSIDERED_ID_TYPES) {
            allIds.addContent(buildElement(idName));
        }

        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(xml));
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
            List<MCRUserAttribute> allMatching = mcrUser
                .getAttributes()
                .stream()
                .filter(a -> a.getName().equals(idName))
                .toList();

            if (allMatching == null || allMatching.isEmpty()) {
                continue;
            }

            String value = allMatching.get(0).getValue();
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
                String realname = mcrUser.getRealName();
                userElement.setAttribute("realname", realname != null ? realname : mcrUser.getUserName());
                userElement.setAttribute("username", mcrUser.getUserName() + "@" + mcrUser.getRealmID());
            }
        });
        return idsOfType;
    }
}
