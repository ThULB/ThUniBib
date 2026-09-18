package de.uni_jena.thunibib.user;

import de.uni_jena.thunibib.ThUniBibCommands;
import jakarta.servlet.http.HttpServletResponse;
import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * @author shermann (Silvio Hermann)
 *
 */
public class TriggerRebuildUsersIndexServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        if (!MCRAccessManager.checkPermission("POOLPRIVILEGE", "administrate-users")) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ThUniBibCommands.rebuildUsersIndex();

        job.getResponse()
            .sendRedirect(MCRFrontendUtil.getBaseURL() + "servlets/solr/select?core=users&XSL.Style=manage-users");
    }
}
