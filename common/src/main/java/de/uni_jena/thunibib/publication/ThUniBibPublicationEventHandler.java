package de.uni_jena.thunibib.publication;

import org.jdom2.Element;
import org.mycore.ubo.publication.PublicationEventHandler;
import org.mycore.user2.MCRUser;

public class ThUniBibPublicationEventHandler extends PublicationEventHandler {

    @Override
    protected void handleName(Element modsNameElement) {
        super.handleName(modsNameElement);
    }

    @Override
    protected void handleUser(Element modsName, MCRUser user) {
        super.handleUser(modsName, user);
    }
}
