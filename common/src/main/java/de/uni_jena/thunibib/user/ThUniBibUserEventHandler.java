package de.uni_jena.thunibib.user;

import de.uni_jena.thunibib.solr.InputDocumentForMCRUserFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandler;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;
import org.mycore.user2.MCRUser;

import java.io.IOException;

/**
 * Event handler to update solr's user index when users are created, modified or deleted.
 *
 * @author shermann (Silvio Hermann)
 */
public class ThUniBibUserEventHandler implements MCREventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void doHandleEvent(MCREvent evt) throws MCRException {
        if (MCREvent.ObjectType.USER == evt.getObjectType()) {
            MCRUser user = (MCRUser) evt.get(MCREvent.USER_KEY);
            switch (evt.getEventType()) {
                case CREATE, UPDATE -> handleUpdate(evt, user);
                case DELETE -> handleDelete(evt, user);
                default -> LOGGER.info("Event Type '{}' is not valid for {}", evt.getEventType(), getClass().getName());
            }
        }
    }

    private void handleDelete(MCREvent evt, MCRUser user) {
        String indexId = user.getUserName() + "@" + user.getRealmID();
        LOGGER.info("{} user {} from index", StringUtils.capitalize(evt.getEventType().toString()), indexId);

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.deleteByQuery("+id:" + indexId);

        updateRequest.setCommitWithin(MCRConfiguration2.getInt("MCR.Solr.commitWithIn").orElse(10));
        MCRSolrAuthenticationManager.obtainInstance()
            .applyAuthentication(updateRequest, MCRSolrAuthenticationLevel.INDEX);
        try {
            updateRequest.process(MCRSolrCoreManager.get("users").get().getClient());
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Error while deleting user {} from index", indexId, e);
        }
    }

    private void handleUpdate(MCREvent evt, MCRUser user) {
        String indexId = user.getUserName() + "@" + user.getRealmID();
        LOGGER.info("{} user {}", evt.getEventType(), indexId);

        UpdateRequest updateRequest = new UpdateRequest("/update");
        updateRequest.setCommitWithin(MCRConfiguration2.getInt("MCR.Solr.commitWithIn").orElse(10));
        MCRSolrAuthenticationManager.obtainInstance()
            .applyAuthentication(updateRequest, MCRSolrAuthenticationLevel.INDEX);

        SolrInputDocument inputDocument = InputDocumentForMCRUserFactory.create(user);
        updateRequest.add(inputDocument);

        try {
            updateRequest.process(MCRSolrCoreManager.get("users").get().getClient());
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Error while {}ing user {} in index", evt.getEventType(), indexId, e);
        }
    }

    @Override
    public void undoHandleEvent(MCREvent evt) throws MCRException {
        LOGGER.error("An error has occurred while updating user in index");
    }
}
