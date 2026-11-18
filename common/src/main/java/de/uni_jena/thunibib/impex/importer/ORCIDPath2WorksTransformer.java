package de.uni_jena.thunibib.impex.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.orcid2.MCRORCIDUtils;
import org.mycore.orcid2.client.exception.MCRORCIDRequestException;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.mycore.orcid2.v3.work.MCRORCIDWorkUtils;
import org.orcid.jaxb.model.v3.release.record.Work;
import org.orcid.jaxb.model.v3.release.record.summary.WorkGroup;
import org.orcid.jaxb.model.v3.release.record.summary.WorkSummary;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class ORCIDPath2WorksTransformer extends MCRContentTransformer implements Comparator<WorkSummary> {

    protected Logger LOGGER = LogManager.getLogger();

    protected final String ORCID_REGEX = "\\d{4}-\\d{4}-\\d{4}-\\d{4}";

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        List<Work> workList = new ArrayList<>();

        HashMap<String, ArrayList<String>> orcidWorkMap = getORCIDWorkMap(source.asString().split("\\s"));
        orcidWorkMap.keySet().forEach(orcid -> {
            try {
                Works works = MCRORCIDClientHelper
                    .getClientFactory()
                    .createReadClient()
                    .fetch(orcid, MCRORCIDSectionImpl.WORKS, Works.class);

                works.getWorkGroup()
                    .stream()
                    .map(WorkGroup::getWorkSummary)
                    .filter(wg -> wg.size() > 0)
                    .forEach(workSummaries -> {
                        workSummaries.sort(this);
                        WorkSummary workSummary = workSummaries.get(0);

                        Work work = MCRORCIDClientHelper
                            .getClientFactory()
                            .createReadClient()
                            .fetch(orcid, MCRORCIDSectionImpl.WORK, Work.class, workSummary.getPutCode());

                        String putCode = String.valueOf(work.getPutCode());

                        if (orcidWorkMap.containsKey(orcid) && orcidWorkMap.get(orcid).contains(putCode)) {
                            workList.add(work);
                        }
                    });
            } catch (MCRORCIDRequestException e) {
                LOGGER.warn("Could not get works for ORCID {}. {}", orcid, e.getMessage());
            }
        });

        List<Element> workElements = MCRORCIDWorkUtils
            .buildUnmergedMODSFromWorks(workList)
            .stream()
            .map(this::stripORCIDIdentifier)
            .toList();
        Element modsCollection = MCRORCIDUtils.buildMODSCollection(workElements);

        return new MCRJDOMContent(modsCollection);
    }

    private Element stripORCIDIdentifier(Element mods) {
        XPATH_FACTORY.compile("mods:identifier[@type='orcid:work']", Filters.element(), null, MODS_NAMESPACE)
            .evaluate(mods)
            .forEach(Element::detach);

        return mods;
    }

    private HashMap<String, ArrayList<String>> getORCIDWorkMap(String[] orcidPaths) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();

        for (String op : orcidPaths) {
            String orcid = MCRXMLFunctions.getMatchingString(op, ORCID_REGEX);
            int workIndex = op.lastIndexOf("/");

            if (workIndex == -1) {
                continue;
            }

            String work = op.substring(workIndex + 1);
            if (!map.containsKey(orcid)) {
                ArrayList<String> works = new ArrayList<>();
                map.put(orcid, works);
            }
            map.get(orcid).add(work);
        }

        return map;
    }

    @Override
    public int compare(WorkSummary ws1, WorkSummary ws2) {
        int v = 0;
        try {
            v = Integer.parseInt(ws1.getDisplayIndex()) - Integer.parseInt(
                ws2.getDisplayIndex());
        } catch (Exception e) {
            LOGGER.error("Could not compare WorkSummaries", e);
        }
        return v;
    }
}
