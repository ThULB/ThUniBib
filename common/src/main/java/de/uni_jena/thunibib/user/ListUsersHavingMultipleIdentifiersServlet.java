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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author shermann (Silvio Hermann)
 *
 */
public class ListUsersHavingMultipleIdentifiersServlet extends MCRServlet {

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
        Element allIds = new Element("thunibib-multiple-identifiers");
        xml.setRootElement(allIds);

        for (String idName : CONSIDERED_ID_TYPES) {
            buildElement(idName).ifPresent(allIds::addContent);
        }

        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(xml));
    }

    /**
     *
     * @param idName
     * @return
     */
    private Optional<Element> buildElement(String idName) {
        EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = manager.createQuery(
            "SELECT userA FROM MCRUser userA JOIN userA.attributes ua GROUP BY userA.internalID, ua.name HAVING count(ua.name) > 1 AND ua.name=:name");
        query.setParameter("name", idName);

        List<MCRUser> resultList = query.getResultList();

        if (resultList.isEmpty()) {
            return Optional.empty();
        }

        Element idElement = new Element("identifier");
        idElement.setAttribute("type", idName);

        for (MCRUser mcrUser : resultList) {
            Element userElement = new Element("user");
            idElement.addContent(userElement);
            String realname = mcrUser.getRealName();
            userElement.setAttribute("realname", realname != null ? realname : mcrUser.getUserName());
            userElement.setAttribute("username", mcrUser.getUserName() + "@" + mcrUser.getRealmID());

            String ids = mcrUser.getAttributes()
                .stream().filter(attr -> idName.equals(attr.getName()))
                .map(MCRUserAttribute::getValue)
                .collect(Collectors.joining(", "));
            userElement.setAttribute("values", ids);
        }
        return Optional.of(idElement);
    }
}
