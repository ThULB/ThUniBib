package de.uni_jena.thunibib.solr;

import org.apache.solr.common.SolrInputDocument;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;

import java.util.SortedSet;

/**
 * Central place for creating a {@link SolrInputDocument} for a given {@link MCRUser}.
 *
 * @author shermann (Silvio Hermann)
 * */
public class InputDocumentForMCRUserFactory {
    private InputDocumentForMCRUserFactory() {
    }

    /**
     * Creates a {@link SolrInputDocument} for a {@link MCRUser}.
     *
     * @param user the user to process
     *
     * @return the {@link SolrInputDocument} for the given {@link MCRUser}
     * */
    public static SolrInputDocument create(MCRUser user) {
        SolrInputDocument inputDocument = new SolrInputDocument();
        inputDocument.addField("id", user.getUserName() + "@" + user.getRealmID());

        String realName = user.getRealName();
        if (realName != null) {
            inputDocument.addField("realName", realName);
        }

        String mail = user.getEMailAddress();
        if (mail != null) {
            inputDocument.addField("mail", mail);
        }

        inputDocument.addField("realmId", user.getRealmID());
        inputDocument.addField("username", user.getUserName());

        SortedSet<MCRUserAttribute> attributes = user.getAttributes();
        attributes
            .forEach(attr -> {
                inputDocument.addField("attribute." + attr.getName(), attr.getValue());
                if ("mail".equals(attr.getName())) {
                    inputDocument.addField("mail", mail);
                }
                if (attr.getName().startsWith("id_")) {
                    inputDocument.addField("nameIdentifier", attr.getValue());
                }
            });

        return inputDocument;
    }
}
