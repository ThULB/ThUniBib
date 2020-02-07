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

    public Stream<OAIRecord> getAll(Date from, Date until) {
        final String fromString = from != null ? new SimpleDateFormat(OAI_SIMPLE_FORMAT).format(from) : null;
        final String untilString = until != null ? new SimpleDateFormat(OAI_SIMPLE_FORMAT).format(until) : null;
        return HarvesterUtil.streamHeaders(harvester, format, fromString, untilString, setSpec).map(h -> {
            try {
                return new OAIRecord(harvester.getRecord(h.getId(), format));
            } catch (Throwable e) {
                LOGGER.error("Error while downloading: " + h.getId(), e);
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
