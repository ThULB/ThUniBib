/**
 *
 */
package de.uni_jena.thunibib.impex.servlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobStatus;
import org.mycore.services.queuedjob.MCRJob_;

/**
 * @author shermann (Silvio Hermann)
 *
 */
public class JobStatusServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private final static Logger LOGGER = LogManager.getLogger(JobStatusServlet.class);

    @Override
    protected void doGetPost(MCRServletJob servletJob) throws Exception {
        if (!MCRXMLFunctions.isCurrentUserInRole("admin")) {
            servletJob.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String deleteJob = servletJob.getRequest().getParameter("deleteJob");
        if (deleteJob != null && MCRXMLFunctions.isCurrentUserInRole("admin")) {
            deleteJob(Long.valueOf(deleteJob));
            servletJob.getResponse()
                .sendRedirect(MCRFrontendUtil.getBaseURL() + servletJob.getRequest().getServletPath());
            return;
        }

        String deleteAll = servletJob.getRequest().getParameter("deleteAll");
        if (deleteAll != null && "true".equals(deleteAll) && MCRXMLFunctions.isCurrentUserInRole("admin")) {
            deleteAllJobs();
            servletJob.getResponse()
                .sendRedirect(MCRFrontendUtil.getBaseURL() + servletJob.getRequest().getServletPath());
            return;
        }

        int start = 0;
        int rows = 10;

        String statusRestriction = null;
        String actionRestriction = null;
        try {
            start = Integer.valueOf(servletJob.getRequest().getParameter("start"));
            rows = Integer.valueOf(servletJob.getRequest().getParameter("rows"));
            statusRestriction = servletJob.getRequest().getParameter("statusRestriction");
            actionRestriction = servletJob.getRequest().getParameter("actionRestriction");
        } catch (RuntimeException re) {
            LOGGER.debug("Could not parse values for 'row' and 'start'. Will use defaults.");
        }

        Element jobs = new Element("MCRJobs");
        Document xml = new Document(jobs);
        Element properties = new Element("properties");
        jobs.addContent(properties);
        properties.addContent(new Element("start").setText(String.valueOf(start)));
        properties.addContent(new Element("rows").setText(String.valueOf(rows)));

        // get available actions
        List<Class> actions = new ArrayList<>();
        EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
        Query q = manager.createQuery("SELECT DISTINCT action FROM MCRJob");
        try {
            actions = q.getResultList();
        } catch (NoResultException e) {
            LOGGER.warn(e);
        }

        // add the available actions
        Element actionsElem = new Element("actions");
        jobs.addContent(actionsElem);
        for (Class a : actions) {
            actionsElem.addContent(new Element("action").setText(a.getName()));
        }

        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<MCRJob> criteria = builder.createQuery(MCRJob.class);
        Root<MCRJob> root = criteria.from(MCRJob.class);
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (actionRestriction != null && actionRestriction.length() > 0) {
            properties.addContent(new Element("actionRestriction").setText(actionRestriction));
            predicates.add(builder.equal(root.get(MCRJob_.action), Class.forName(actionRestriction)));
        }

        if (statusRestriction != null && statusRestriction.length() > 0) {
            properties.addContent(new Element("statusRestriction").setText(statusRestriction));
            predicates.add(builder.equal(root.get(MCRJob_.status), MCRJobStatus.valueOf(statusRestriction)));
        }

        criteria.where(predicates.toArray(new Predicate[] {}));
        criteria.orderBy(builder.desc(root.get(MCRJob_.start)));

        // get hit count for query above
        long hits = manager.createQuery(criteria).getResultList().size();
        properties.addContent(new Element("hits").setText(String.valueOf(hits)));

        List<MCRJob> results = manager.createQuery(criteria).setFirstResult(start).setMaxResults(rows).getResultList();

        for (MCRJob mcrJob : results) {
            Element jobElem = new Element("MCRJob");

            // get the additional job parameters
            Element paramsElem = new Element("parameters");
            for (String key : mcrJob.getParameters().keySet()) {
                paramsElem
                    .addContent(new Element("parameter").setAttribute("name", key).setText(mcrJob.getParameter(key)));
            }

            // avoid an empty param element
            if (paramsElem.getChildren().size() > 0) {
                jobElem.addContent(paramsElem);
            }

            // add job properties
            jobElem.addContent(new Element("id").setText(String.valueOf(mcrJob.getId())));
            jobElem.addContent(new Element("action").setText(mcrJob.getAction().getName()));
            jobElem.addContent(new Element("action-simple").setText(mcrJob.getAction().getSimpleName()));

            Date started = mcrJob.getStart();
            Date finished = mcrJob.getFinished();

            DateFormat dateFormater = new SimpleDateFormat("YYYY-MM-dd HH:mm", Locale.ROOT);

            if (started != null) {
                jobElem.addContent(new Element("started").setText(dateFormater.format(started)));
            }
            if (finished != null) {
                jobElem.addContent(new Element("finished").setText(dateFormater.format(finished)));
            }
            jobElem.addContent(new Element("status").setText(mcrJob.getStatus().name()));

            if (started != null && finished != null) {
                long duration = finished.getTime() - started.getTime();
                jobElem.addContent(new Element("duration").setText(String.valueOf(duration)));
            }

            jobs.addContent(jobElem);
        }
        MCRJDOMContent content = new MCRJDOMContent(xml);
        getLayoutService().doLayout(servletJob.getRequest(), servletJob.getResponse(), content);
    }

    private void deleteAllJobs() {
        EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<MCRJob> criteria = builder.createQuery(MCRJob.class);
        Root<MCRJob> root = criteria.from(MCRJob.class);
        criteria.where(builder.greaterThan(root.get(MCRJob_.id).as(Long.class), 0L));
        manager.createQuery(criteria).getResultStream().forEach(mcrJob -> manager.remove(mcrJob));
    }

    private void deleteJob(long jobid) {
        EntityManager manager = MCREntityManagerProvider.getCurrentEntityManager();
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<MCRJob> criteria = builder.createQuery(MCRJob.class);
        Root<MCRJob> root = criteria.from(MCRJob.class);
        criteria.where(builder.equal(root.get(MCRJob_.id), jobid));
        MCRJob mcrJob = manager.createQuery(criteria).getSingleResult();
        manager.remove(mcrJob);
    }
}
