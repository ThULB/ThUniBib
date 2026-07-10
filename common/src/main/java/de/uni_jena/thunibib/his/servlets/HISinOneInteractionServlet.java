package de.uni_jena.thunibib.his.servlets;

import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.cli.HISinOneCommands;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

public class HISinOneInteractionServlet extends MCRServlet {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        if (MCRXMLFunctions.isCurrentUserInRole("administrator")) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String id = job.getRequest().getParameter("id");
        if (id == null || id.isEmpty()) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter 'id'");
        }

        String action = job.getRequest().getParameter("action");
        if (action == null) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter 'action'");
            return;
        }

        SysValue sysValue = switch (action) {
            case "publish" -> HISinOneCommands.publish(id);
            case "update" -> HISinOneCommands.update(id);
            case "delete" -> HISinOneCommands.remove(id);
            default -> SysValue.UnresolvedSysValue;
        };

        LOGGER.info("{} of {} resulted in {}", action, id, sysValue);
        job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "receive/" + id);
    }
}
