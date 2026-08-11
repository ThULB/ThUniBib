package de.uni_jena.thunibib.solr;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTransactionManager;
import org.mycore.frontend.servlets.MCRServlet;

import java.io.IOException;
import java.util.Locale;

public class SolrServletFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws ServletException, IOException {
        HttpServletRequest servletRequest = (HttpServletRequest) req;
        String userAgent = servletRequest.getHeader("User-Agent");

        if (userAgent == null || userAgent.toLowerCase(Locale.ROOT).indexOf("bot") > -1) {
            LOGGER.warn("User-Agent '{}' was blocked from accessing SOLR", userAgent);
            ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String core = req.getParameter("core");
        if (core != null && "users".equals(core)) {
            if (checkPermission(servletRequest, "POOLPRIVILEGE", "administrate-users")) {
                chain.doFilter(req, resp);
            } else {
                ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private boolean checkPermission(HttpServletRequest servletRequest, String id, String permission) {
        MCRSession session = MCRServlet.getSession(servletRequest);
        MCRSessionMgr.setCurrentSession(session);
        MCRTransactionManager.beginTransactions();

        try {
            return MCRAccessManager.checkPermission(id, permission);
        } finally {
            MCRTransactionManager.commitTransactions();
            MCRSessionMgr.releaseCurrentSession();
        }
    }
}
