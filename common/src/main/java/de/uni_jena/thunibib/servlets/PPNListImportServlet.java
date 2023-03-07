package de.uni_jena.thunibib.servlets;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import de.uni_jena.thunibib.enrichment.EnrichmentByAffiliationCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.i18n.MCRTranslation;

public class PPNListImportServlet extends MCRServlet {

    private static Logger LOGGER = LogManager.getLogger(PPNListImportServlet.class);

    @Override
    protected void doGet(MCRServletJob job) throws Exception {
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            job.getResponse()
                .sendError(HttpServletResponse.SC_FORBIDDEN,
                    MCRTranslation.translate("component.base.webpage.notLoggedIn"));
            return;
        }

        String list = job.getRequest().getParameter("list");
        if (list == null) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        StringBuilder sb = new StringBuilder();

        Arrays.stream(list.split("\\s"))
            .filter(ppn -> ppn.trim().length() > 0)
            .forEach(ppn -> {
                LOGGER.info("Importing ppn {}", ppn);
                try {
                    EnrichmentByAffiliationCommands.enrichOrCreateByPPN(ppn, "imported", "supportedmods");
                    sb.append(ppn + " ");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
        job.getResponse().setContentType("application/json");
        job.getResponse().getWriter().println("{\"list\":[" + sb.toString().trim().replace(" ", ",") + "]}");
    }
}
