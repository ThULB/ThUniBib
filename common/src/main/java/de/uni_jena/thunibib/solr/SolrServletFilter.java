package de.uni_jena.thunibib.solr;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTransactionManager;
import org.mycore.frontend.servlets.MCRServlet;

import java.io.IOException;

public class SolrServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws ServletException, IOException {

        String core = req.getParameter("core");

        if (core != null && "users".equals(core)) {
            HttpServletRequest servletRequest = (HttpServletRequest) req;

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
