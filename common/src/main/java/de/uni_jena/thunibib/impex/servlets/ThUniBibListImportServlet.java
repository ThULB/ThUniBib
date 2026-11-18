package de.uni_jena.thunibib.impex.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueueManager;
import org.mycore.ubo.AccessControl;
import org.mycore.ubo.importer.DozBibImportServlet;
import org.mycore.ubo.importer.ImportListJobAction;
import org.mycore.ubo.importer.ListImportJob;
import org.mycore.user2.MCRUserManager;

import java.util.Optional;

import static org.mycore.ubo.importer.ImportListJobAction.EDITOR_SUBMISSION_PARAMETER;
import static org.mycore.ubo.importer.ImportListJobAction.USER_ID_PARAMETER;

/**
 *
 * @see org.mycore.ubo.importer.DozBibImportServlet
 * @see org.mycore.ubo.importer.ImportJob
 * */
public class ThUniBibListImportServlet extends DozBibImportServlet {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletResponse resp = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        HttpServletRequest req = job.getRequest();

        Element formInput = getImportJobFormInput(req);
        if (formInput == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            MCRJob mcrJob = new MCRJob(ImportListJobAction.class);
            mcrJob.setParameter(EDITOR_SUBMISSION_PARAMETER, new XMLOutputter().outputString(formInput));
            mcrJob.setParameter(USER_ID_PARAMETER,
                MCRUserManager.getCurrentUser().getUserName() + "@" + MCRUserManager.getCurrentUser().getRealmID());
            MCRJobQueueManager.getInstance().getJobQueue(ImportListJobAction.class).offer(mcrJob);

            Element export = new Element("export");
            export.setAttribute("submitted", "true");

            req.setAttribute("XSL.Style", "with-import");
            getLayoutService().doLayout(req, resp, new MCRJDOMContent(export));
        } catch (Exception e) {
            LOGGER.error("Error during list import", e);
        }
    }

    /**
     * Builds the required form data element for the import job.
     * <br/>
     * Format of {@code formInput} required by {@link ListImportJob}:
     *
     * <pre>
     * {@code
     * <import targetType="preview-html" sourceType="PPNList" enrich="true">
     *   <partOf>true</partOf>
     *   <status>confirmed</status>
     *   <ubogenre>preface</ubogenre>
     *   <origin>1010.03</origin>
     *   <DataSources type="provided" />
     *   <DataSources type="custom" />
     *   <source>1877507490 1877519626</source>
     * </import>
     * }
     *</pre>
     * @param req the request
     *
     * @see org.mycore.ubo.importer.DozBibImportServlet
     * */
    protected final Element getImportJobFormInput(HttpServletRequest req) {
        String sourceType = req.getParameter("import.sourceType");
        String[] parameterValues = req.getParameterValues(sourceType);

        if (parameterValues == null || parameterValues.length == 0) {
            return null;
        }

        String identifiers = String.join(" ", parameterValues);
        boolean enrich = Boolean.parseBoolean(req.getParameter("import.enrich"));

        Element formInput = new Element("import");
        formInput.setAttribute("targetType", "import");
        formInput.setAttribute("sourceType", sourceType);
        formInput.setAttribute("enrich", String.valueOf(enrich));
        formInput.addContent(new Element("source").setText(identifiers));

        Optional<String> partOf = Optional.ofNullable(req.getParameter("partOf"));
        if (partOf.isPresent()) {
            formInput.addContent(new Element("partOf").setText(partOf.get()));
        }

        Optional<String> status = Optional.ofNullable(req.getParameter("status"));
        if (status.isPresent()) {
            formInput.addContent(new Element("status").setText(status.get()));
        }

        Optional<String> ubogenre = Optional.ofNullable(req.getParameter("ubogenre"));
        if (ubogenre.isPresent()) {
            formInput.addContent(new Element("ubogenre").setText(ubogenre.get()));
        }

        Optional<String> origin = Optional.ofNullable(req.getParameter("origin"));

        if (origin.isPresent()) {
            formInput.addContent(new Element("origin").setText(origin.get()));
        }

        Optional<String> provided = Optional.ofNullable(req.getParameter("DataSources.type.provided"));
        Element providedElement = new Element("DataSources").setAttribute("type", "provided");
        formInput.addContent(providedElement);
        if (provided.isPresent()) {
            providedElement.setText(provided.get());
        }

        Optional<String> custom = Optional.ofNullable(req.getParameter("DataSources.type.custom"));
        Element customElement = new Element("DataSources").setAttribute("type", "custom");
        formInput.addContent(customElement);
        if (custom.isPresent()) {
            customElement.setText(custom.get());
        }

        return formInput;
    }
}
