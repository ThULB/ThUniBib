package de.uni_jena.thunibib.impex;

import de.uni_jena.thunibib.ThUniBibMailer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.mycore.common.MCRConstants.*;

public class ThUniBibImportJobAction extends MCRJobAction {

    protected static final Logger LOGGER = LogManager.getLogger(ThUniBibImportJobAction.class);
    protected static final String SRU_CATALOG = MCRConfiguration2.getString("MCR.PICA2MODS.DATABASE").orElse("gvk");
    protected static final int BATCH_SIZE = 10;

    public ThUniBibImportJobAction(MCRJob job) {
        super(job);
    }

    @Override
    public void execute() throws ExecutionException {
        String query = job.getParameters().get("query");
        String status = job.getParameters().get("status");
        String filter = job.getParameters().get("filter");

        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        try {
            mcrSession.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
            MCRTransactionHelper.beginTransaction();

            List<String> ppns = getList(new ArrayList<>(), query, 1);
            LOGGER.info("Found a total of {} ppn matching query {}", ppns.size(), query);

            List<MCRObject> objects = new ArrayList<>();
            String importId = String.valueOf(job.getId());

            int ignoreCount = 0;
            for (String ppn : ppns) {
                if (!EnrichmentByAffiliationCommands.isAlreadyStored("id_ppn", ppn)) {
                    MCRObject mcrobj = EnrichmentByAffiliationCommands.enrichOrCreateByPPNWithKey(ppn, status, filter,
                        importId);

                    if (mcrobj != null) {
                        objects.add(mcrobj);
                    } else {
                        LOGGER.warn("Could create MCRObject for ppn {}", ppn);
                    }
                } else {
                    LOGGER.info("Ignoring ppn {} as it would create a duplicate", ppn);
                    ignoreCount++;
                }
            }

            job.setParameter("size-total", String.valueOf(ppns.size()));
            job.setParameter("size-ignored", String.valueOf(ignoreCount));

            try {
                ThUniBibMailer.sendMail(importId, objects, status, SRU_CATALOG.toUpperCase());
            } catch (Exception e) {
                LOGGER.error("Could not send e-mail for import job {} {}", job.getId(), query, e);
            }
        } catch (Throwable throwable) {
            LOGGER.error("Could not run {} for query {}", name(), query, throwable);
            MCRTransactionHelper.rollbackTransaction();
            // MCRJob remains in state PROCESSING and will be reset to NEW after MCR.QueuedJob.TimeTillReset minutes
            throw throwable;
        } finally {
            if (MCRTransactionHelper.isTransactionActive()) {
                MCRTransactionHelper.commitTransaction();
            }
            MCRSessionMgr.releaseCurrentSession();
        }
    }

    private List<String> getList(List<String> ppns, String query, int start) {
        String url = EnrichmentByAffiliationCommands.buildRequestURL(query, String.valueOf(start));
        Element result = Objects.requireNonNull(MCRURIResolver.instance().resolve(url));

        XPathExpression<Element> r = XPATH_FACTORY.compile(".//mods:mods/mods:recordInfo/mods:recordIdentifier",
            Filters.element(), null, MCRConstants.ZS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
        List<Element> recordIdentifierElements = r.evaluate(result);
        String numberOfRecordsStr = result.getChildTextNormalize("numberOfRecords", MCRConstants.ZS_NAMESPACE);

        if (numberOfRecordsStr == null) {
            return new ArrayList<>();
        }

        recordIdentifierElements.stream().map(Element::getTextNormalize).forEach(ppn -> ppns.add(ppn));
        if ((start + BATCH_SIZE) < Integer.parseInt(numberOfRecordsStr)) {
            getList(ppns, query, start + BATCH_SIZE);
        }
        return ppns;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void rollback() {
        LOGGER.info("rollback()");
    }
}
