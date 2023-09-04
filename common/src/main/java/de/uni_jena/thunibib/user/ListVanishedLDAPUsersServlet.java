/**
 *
 */
package de.uni_jena.thunibib.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.ThUniBibCommands;
import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.user2.MCRRealm;
import org.mycore.user2.MCRRealmFactory;

/**
 * @author shermann (Silvio Hermann)
 *
 */
public class ListVanishedLDAPUsersServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(MCRServletJob job) throws Exception {
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<String> realmIDs = MCRRealmFactory.listRealms()
            .stream()
            .filter(mcrRealm -> !mcrRealm.equals(MCRRealmFactory.getLocalRealm()))
            .map(MCRRealm::getID)
            .collect(Collectors.toList());

        Document xml = new Document(new Element("vanished-ldap-users"));
        realmIDs.forEach(r -> {
            ThUniBibCommands.listVanishedLDAPUsers(r).forEach(user -> {
                Element userElem = new Element("user");
                userElem.setAttribute(new Attribute("realm", user.getRealmID()));
                userElem.addContent(new Element("name").setText(user.getUserName()));
                userElem.addContent(new Element("id").setText(user.getUserID()));
                userElem.addContent(new Element("realName").setText(user.getRealName()));

                Element attributes = new Element("attributes");
                userElem.addContent(attributes);
                user.getAttributes().forEach(attr -> {
                    attributes.addContent(new Element(attr.getName()).setText(attr.getValue()));
                });

                xml.getRootElement().addContent(userElem);
            });
        });

        MCRJDOMContent content = new MCRJDOMContent(xml);
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), content);
    }

    @Override
    protected void doPost(MCRServletJob job) throws Exception {
        if (!MCRXMLFunctions.isCurrentUserSuperUser()) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        boolean status = false;

        Optional<String> username = job.getRequest().getParameterMap().keySet().stream().findFirst();
        if (username.isPresent()) {
            status = ThUniBibCommands.moveUserToRealm(username.get().replace("\"", ""),
                MCRRealmFactory.getLocalRealm().getID());
        }

        JsonObject json = new JsonObject();
        json.addProperty("status", status);
        job.getResponse().getWriter().println(json.toString());
        job.getResponse().getWriter().flush();
    }
}
