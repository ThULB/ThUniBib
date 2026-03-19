package de.uni_jena.thunibib.common.xml;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.user2.MCRUser;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.util.List;

/**
 * Invocation: <code>orcidStatistics:trusted-party</code>.
 *
 * @author shermann (Silvio Hermann)
 */
public class ORCIDStatisticsResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            return new JDOMSource(new Document(new Element("orcid-statistics")));
        }

        try {
            Element root = new Element("orcid-statistics");
            Element trustedParty = new Element("trusted-party");
            root.addContent(trustedParty);
            Document xml = new Document(root);

            EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
            Query query = manager.createQuery(
                "SELECT userA FROM MCRUser userA JOIN userA.attributes ua WHERE ua.name LIKE :likeQuery");
            query.setParameter("likeQuery", "orcid_credential_%");

            List<MCRUser> list = query.getResultList();
            for (MCRUser user : list) {
                Element e = new Element("user");
                String id = user.getUserName() + "@" + user.getRealmID();
                e.setAttribute("name", id);
                e.setAttribute("realName", user.getRealName() != null ? user.getRealName() : id);

                Element attributes = new Element("attributes");
                e.addContent(attributes);
                user.getAttributes().forEach(attr -> {
                    Element a = new Element("attribute");
                    a.setAttribute("name", attr.getName());
                    a.setAttribute("value", attr.getValue());
                    attributes.addContent(a);
                });

                trustedParty.addContent(e);
            }
            return new JDOMSource(xml);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}
