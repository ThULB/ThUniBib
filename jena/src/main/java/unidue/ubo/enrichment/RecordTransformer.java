package unidue.ubo.enrichment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRClassTools;
import org.mycore.oai.pmh.CannotDisseminateFormatException;
import org.mycore.oai.pmh.Header;
import org.mycore.oai.pmh.IdDoesNotExistException;
import org.mycore.oai.pmh.Record;
import org.mycore.oai.pmh.harvester.HarvestException;
import org.mycore.oai.pmh.harvester.Harvester;
import org.mycore.oai.pmh.harvester.HarvesterBuilder;
import org.mycore.oai.pmh.harvester.HarvesterUtil;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RecordTransformer {

    public static final String OAI_SIMPLE_FORMAT = "yyyy-MM-dd";

    private static final Logger LOGGER = LogManager.getLogger(RecordTransformer.class);

    private static final String DEFAULT_FORMAT = "oai_dc";

    private static TransformerFactory factory = TransformerFactory
            .newInstance("net.sf.saxon.TransformerFactoryImpl", MCRClassTools
                    .getClassLoader());

    private final String baseURL;

    private String format;

    private String setSpec;

    private Harvester harvester;

    private Function<String, String> formatFunc = (format) -> {
        if (format == null || !harvester.listMetadataFormats().stream().anyMatch(mf -> mf.getPrefix().equals(format))) {
            LOGGER.warn("Metadata format \"{}\" isn't supported fallback to default format (\"{}\").", format,
                    DEFAULT_FORMAT);
            return DEFAULT_FORMAT;
        } else {
            return format;
        }
    };

    public RecordTransformer(String baseURL, String format, String setSpec) {
        this(baseURL, format, setSpec, true);
    }

    public RecordTransformer(String baseURL, String format, String setSpec, boolean skipFormatCheck) {
        this.baseURL = baseURL;
        this.setSpec = setSpec;
        harvester = HarvesterBuilder.createNewInstance(this.baseURL);
        this.format = !skipFormatCheck ? formatFunc.apply(format) : format;
    }

    public OAIRecord transform(String id) throws CannotDisseminateFormatException, HarvestException,
            IdDoesNotExistException, MalformedURLException {
        return transform(id, null);
    }

    public OAIRecord transform(String id, String stylesheet) throws CannotDisseminateFormatException,
            HarvestException, IdDoesNotExistException {

        Record r = harvester.getRecord(id, format);

        if (r.getMetadata() == null) {
            LOGGER.warn("No metadata found for record with id {}.", id);
            return null;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((new XMLOutputter(Format.getPrettyFormat())).outputString(r.getMetadata().toXML()));
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("recordBaseURL", baseURL);
            params.put("recordId", id);

            JDOMResult result = transform(r.getMetadata().toXML(),
                    Optional.ofNullable(stylesheet).orElse("/transformer/" + format + ".xsl"), params);

            OAIRecord record = new OAIRecord(r);
            record.setDocument(result.getDocument());

            return record;
        } catch (TransformerException | IOException e) {
            throw new IllegalArgumentException(
                    "Couldn't transform metadata with provided xsl stylesheet ("
                            + Optional.ofNullable(stylesheet).orElse("/transformer/" + format + ".xsl") + ").",
                    e);
        }
    }

    public Stream<OAIRecord> transformAll(String stylesheet, Date from, Date until) {
        final String fromString = from != null ? new SimpleDateFormat(OAI_SIMPLE_FORMAT).format(from) : null;
        final String untilString = until != null ? new SimpleDateFormat(OAI_SIMPLE_FORMAT).format(until) : null;
        return processRecords(HarvesterUtil.streamHeaders(harvester, format, fromString, untilString, setSpec),
                stylesheet);
    }

    private JDOMResult transform(Element xml, String stylesheet, Map<String, Object> params)
            throws TransformerException, IOException {
        InputStream xis = getClass().getResourceAsStream(stylesheet);
        if (xis == null) {
            xis = MCRClassTools
                    .getClassLoader().getResourceAsStream("xsl/" + stylesheet);
        }

        Source xslt = new StreamSource(xis);
        Transformer transformer = factory.newTransformer(xslt);
        Optional.ofNullable(params).ifPresent(p -> p.forEach(transformer::setParameter));
        JDOMResult result = new JDOMResult();
        transformer.transform(new JDOMSource(xml), result);

        xis.close();

        return result;
    }

    public Stream<OAIRecord> processRecords(Stream<Header> recordStream, String stylesheet) {
        return recordStream.parallel().map(h -> {
            try {
                return this.transform(h.getId(), stylesheet);
            } catch (Throwable e) {
                LOGGER.error("Error while downloading: " + h.getId(), e);
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
