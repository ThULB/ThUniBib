package de.uni_jena.thunibib.his.xml;

import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import de.uni_jena.thunibib.his.api.v1.cs.sys.values.SysValue;
import de.uni_jena.thunibib.his.cli.HISinOneCommands;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This resolver allows resolving of internal keys used by HISinOne to address its entities.
 *
 * <p>
 * Usage:
 * </p>
 * <p>
 * {@code hisinone:<resolve|create>:<[requested field]>:<conference|country|creatorType|documentType|journal|publication|publicationAccessType|publicationResource|publicationType|globalIdentifiers|language|peerReviewed|person|publisher|researchAreaKdsf|subjectArea|state|thesisType|visibility>:[value]}
 * </p>
 *
 * Note:
 * <p>
 * The <strong>{@code create:}</strong> uri part is supported for <strong>{@code publisher:}</strong> uri part only.
 * </p>
 *
 * @author shermann (Silvio Hermann)
 * */
public class HISinOneResolver implements URIResolver {
    /**
     * Regular expression matching the date range format used to depict a conference duration (YYYY.MM.dd-dd or YYYY)
     */
    static final String CONFERENCE_DATE_REGEX = "\\d{4}\\.\\d{2}\\.\\d{2}-\\d{2}|\\d{4}";

    protected static final Logger LOGGER = LogManager.getLogger(HISinOneResolver.class);

    private static final Map<String, SysValue.LanguageValue> LANGUAGE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CONFERENCE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> COUNTRY_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CREATOR_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> DOCUMENT_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> IDENTIFIER_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PEER_REVIEWED_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_ACCESS_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_RESOURCE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> PUBLICATION_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> RESEARCH_AREA_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> STATE_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> SUBJECT_AREA_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> THESIS_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> VISIBILITY_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> CONFERENCE_EVENT_TYPE_MAP = new HashMap<>();
    private static final Map<String, SysValue> LICENSE_TYPE_MAP = new HashMap<>();

    public enum Mode {
        resolve, create
    }

    public enum ResolvableTypes {
        conference,
        researchPartner,
        country,
        creatorType,
        documentType,
        globalIdentifiers,
        journal,
        language,
        organization,
        peerReviewed,
        person,
        publication,
        publicationAccessType,
        publicationResource,
        publicationType,
        publisher,
        researchAreaKdsf,
        state,
        subjectArea,
        thesisType,
        visibility,
        license
    }

    /**
     * {@link SimpleDateFormat} used to parse a conference date.
     * */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd");

    /**
     * Resolves the requested field. The result is an {@link Source} containing a {@link Element}.
     *
     * <p>
     *  {@code <int>…</int>}
     * </p>
     *  or
     * <p>
     *  {@code <int><i>…</i> … <i>…</i></int>}
     * </p>
     * */
    @Override
    final public Source resolve(String href, String base) throws TransformerException {
        LOGGER.debug("Resolving '{}'", href);

        String[] parts = href.split(":");

        Mode mode = Mode.valueOf(parts[1]);
        String field = parts[2];
        String entity = parts[3];
        String fromValue;

        if (ResolvableTypes.publicationType.name().equals(entity) || ResolvableTypes.documentType.name()
            .equals(entity)) {
        }

        String idValue = null;
        if (ResolvableTypes.person.name().equals(entity)) {
            idValue = parts[5];
        }

        fromValue = parts.length > 4 ? URLDecoder.decode(parts[4], StandardCharsets.UTF_8) : "";

        var sysValue = switch (ResolvableTypes.valueOf(entity)) {
            case conference -> Mode.resolve.equals(mode) ? resolveConference(fromValue) : createConference(fromValue);
            case country -> resolveCountry(fromValue);
            case researchPartner -> Mode.resolve.equals(mode) ? ResearchPartnerResolver.getInstance().resolve(fromValue) : ResearchPartnerResolver.getInstance().create(fromValue);
            case creatorType -> resolveCreatorType(fromValue);
            case documentType -> resolveDocumentType(fromValue);
            case globalIdentifiers -> resolveIdentifierType(fromValue);
            case journal -> Mode.resolve.equals(mode) ? JournalResolver.getInstance().resolve(fromValue) : createParent(fromValue);
            case language -> resolveLanguage(fromValue);
            case organization -> PersonResolver.getInstance().resolveOrganization(fromValue);
            case peerReviewed -> resolvePeerReviewedType(fromValue);
            case person -> PersonResolver.getInstance().resolvePerson(fromValue, idValue);
            case publication -> Mode.resolve.equals(mode) ? resolvePublication(fromValue) : createParent(fromValue);
            case publicationAccessType -> resolvePublicationAccessType(fromValue);
            case publicationResource -> resolvePublicationResourceType(fromValue);
            case publicationType -> resolvePublicationType(fromValue);
            case publisher -> Mode.resolve.equals(mode) ? PublisherResolver.getInstance().resolve(fromValue) : PublisherResolver.getInstance().create(fromValue);
            case researchAreaKdsf -> resolveResearchAreaKdsf(fromValue);
            case state -> resolveState(fromValue);
            case subjectArea -> resolveSubjectArea(fromValue);
            case thesisType -> resolveThesisType(fromValue);
            case visibility -> resolveVisibility(fromValue);
            case license -> resolveLicense(fromValue);
        };

        if (sysValue instanceof List) {
            List<Integer> resolvedValues = getFieldValues((List<SysValue>) sysValue, field);
            logResolvingResult(href, StringUtils.join(resolvedValues, ", "));
            Element ints = new Element("int");
            resolvedValues.forEach(val -> ints.addContent(new Element("i").setText(String.valueOf(val))));
            return new JDOMSource(ints);
        } else {
            int resolvedValue = getFieldValue((SysValue) sysValue, field);
            logResolvingResult(href, String.valueOf(resolvedValue));
            return new JDOMSource(new Element("int").setText(String.valueOf(resolvedValue)));
        }
    }

    private SysValue resolveConference(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        if (CONFERENCE_TYPE_MAP.containsKey(decodedValue)) {
            return CONFERENCE_TYPE_MAP.get(decodedValue);
        }

        String[] conferenceParts = decodedValue.split(";");

        if (!(conferenceParts.length >= 3)) {
            return SysValue.UnresolvedSysValue;
        }

        String name = conferenceParts[0].trim();
        String location = conferenceParts[1].trim();
        String dateRange = conferenceParts[2].trim();

        Optional<HashMap<String, Long>> startEndeDates = getStartEndeDates(dateRange);
        if (startEndeDates.isEmpty()) {
            return SysValue.ErroneousSysValue;
        }

        long startDate = startEndeDates.get().get("startDate");
        long endDate = startEndeDates.get().get("endDate");

        // Search by name of the conference
        Map<String, String> params = new HashMap<>();
        params.put("q", conferenceParts[0]);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Conference.class), params)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Conference.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Conference[] conferences = response.readEntity(SysValue.Conference[].class);
            Optional<SysValue.Conference> match = Arrays.stream(conferences)
                .filter(conference -> conference.getDefaultText().equals(name))
                .filter(conference -> location.equals(conference.getCity()))
                .filter(conference -> conference.getStartDate() >= startDate && conference.getEndDate() <= endDate)
                .findFirst();
            return match.isPresent() ? match.get() : SysValue.UnresolvedSysValue;
        }
    }

    private SysValue resolveConferenceState(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ConferenceState.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ConferenceState.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.ConferenceState[] states = response.readEntity(SysValue.ConferenceState[].class);
            Optional<SysValue.ConferenceState> match = Arrays.stream(states)
                .filter(state -> state.getDefaultText().equals(decodedValue))
                .findFirst();

            return match.isPresent() ? match.get() : SysValue.UnresolvedSysValue;
        }
    }

    private SysValue createConference(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
        if (CONFERENCE_TYPE_MAP.containsKey(decodedValue)) {
            return CONFERENCE_TYPE_MAP.get(decodedValue);
        }

        String[] conferenceParts = decodedValue.split(";");

        if (conferenceParts.length != 3) {
            return SysValue.UnresolvedSysValue;
        }

        String name = conferenceParts[0].trim();
        String city = conferenceParts[1].trim();
        String dateRange = conferenceParts[2].trim();

        Optional<HashMap<String, Long>> startEndeDates = getStartEndeDates(dateRange);
        if (startEndeDates.isEmpty()) {
            return SysValue.ErroneousSysValue;
        }

        SysValue country = resolveCountry(URLEncoder.encode("ohne Angabe", StandardCharsets.UTF_8));
        SysValue language = resolveLanguage("de");
        SysValue status = resolveConferenceState("validiert");
        SysValue conferenceEventType = resolveConferenceEventTypeValue("vor Ort");

        JsonObject conference = buildConferenceObject(city, conferenceEventType, name, country, language, status,
            startEndeDates.get().get("startDate"), startEndeDates.get().get("endDate"));

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.post(SysValue.resolve(SysValue.Conference.class), conference.toString())) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Conference.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Conference created = response.readEntity(SysValue.Conference.class);

            CONFERENCE_TYPE_MAP.put(decodedValue, created);
            return created;
        } catch (Exception e) {
            LOGGER.error("Could not create conference", e);
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Extracts and parses the start and end dates from a conference date range string.
     * <p>
     * The provided date range must match {@code CONFERENCE_DATE_REGEX}. If the
     * format is valid, the method parses the start and end dates and returns them
     * in a {@link HashMap} with the keys {@code "startDate"} and {@code "endDate"}.
     * If the input does not match the expected format or if date parsing fails,
     * an empty {@link Optional} is returned.
     *
     * @param dateRange the conference date range string to parse
     * @return an {@link Optional} containing a map with the parsed start and end
     *         dates ({@code "startDate"} and {@code "endDate"}) as epoch values,
     *         or {@link Optional#empty()} if the input format is invalid or the
     *         dates cannot be parsed
     */
    private Optional<HashMap<String, Long>> getStartEndeDates(String dateRange) {
        if (!dateRange.matches(CONFERENCE_DATE_REGEX)) {
            LOGGER.error("Conference date '{}' does not match '{}'", dateRange, CONFERENCE_DATE_REGEX);
            return Optional.empty();
        }

        final long startDate, endDate;
        try {
            startDate = getStartDate(dateRange);
            endDate = getEndDate(dateRange);
            HashMap<String, Long> startEndeDates = new HashMap<>();
            startEndeDates.put("startDate", startDate);
            startEndeDates.put("endDate", endDate);
            return Optional.of(startEndeDates);
        } catch (ParseException e) {
            LOGGER.error("Could not parse start or end date ({}) of conference", dateRange, e);
            return Optional.empty();
        }
    }

    private JsonObject buildConferenceObject(String city, SysValue eventType, String defaultText, SysValue country,
        SysValue language, SysValue status, long startDate, long endDate) {
        JsonObject conference = new JsonObject();
        conference.addProperty("city", city);

        JsonObject conferenceEventTypeProp = new JsonObject();
        conferenceEventTypeProp.addProperty("id", eventType.getId());
        conference.add("conferenceEventType", conferenceEventTypeProp);

        conference.addProperty("defaulttext", defaultText);

        JsonObject countryProp = new JsonObject();
        countryProp.addProperty("id", country.getId());
        conference.add("country", countryProp);

        JsonObject languageProp = new JsonObject();
        languageProp.addProperty("id", language.getId());
        conference.add("language", languageProp);

        JsonObject statusProp = new JsonObject();
        statusProp.addProperty("id", status.getId());
        conference.add("status", statusProp);

        conference.addProperty("startDate", startDate);
        conference.addProperty("endDate", endDate);

        return conference;
    }

    protected SysValue resolveCountry(String countryName) {
        String decodedCountryName = URLDecoder.decode(countryName, StandardCharsets.UTF_8);

        if (COUNTRY_TYPE_MAP.containsKey(decodedCountryName)) {
            return COUNTRY_TYPE_MAP.get(decodedCountryName);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Country.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Country.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Country[] sysValue = response.readEntity(SysValue.Country[].class);
            Optional<SysValue.Country> match = Arrays.stream(sysValue)
                .filter(country -> decodedCountryName.equals(country.getDefaultText()))
                .findFirst();

            if (match.isPresent()) {
                COUNTRY_TYPE_MAP.put(decodedCountryName, match.get());
                return match.get();
            }

            return SysValue.UnresolvedSysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    private SysValue resolveConferenceEventTypeValue(String eventType) {
        String decodedConferenceEventType = URLDecoder.decode(eventType, StandardCharsets.UTF_8);

        if (CONFERENCE_EVENT_TYPE_MAP.containsKey(decodedConferenceEventType)) {
            return CONFERENCE_EVENT_TYPE_MAP.get(decodedConferenceEventType);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ConferenceEventTypeValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ConferenceEventTypeValue.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.ConferenceEventTypeValue[] sysValue = response.readEntity(
                SysValue.ConferenceEventTypeValue[].class);
            Optional<SysValue.ConferenceEventTypeValue> match = Arrays.stream(sysValue)
                .filter(conferenceEventTypeValue -> decodedConferenceEventType.equals(
                    conferenceEventTypeValue.getDefaultText()))
                .findFirst();

            if (match.isPresent()) {
                CONFERENCE_EVENT_TYPE_MAP.put(decodedConferenceEventType, match.get());
                return match.get();
            }
            return SysValue.UnresolvedSysValue;
        } catch (Exception e) {
            return SysValue.ErroneousSysValue;
        }
    }

    /**
     * Resolves the <code>publicationResourceType</code> by the given <code>mods:typeofResource</code> category id.
     *
     * @param resourceTypeText
     * @return the resolved his id or the HIS id for default value "Sonstige Darstellungsform"
     */
    private SysValue resolvePublicationResourceType(String resourceTypeText) {
        if (PUBLICATION_RESOURCE_TYPE_MAP.containsKey(resourceTypeText)) {
            return PUBLICATION_RESOURCE_TYPE_MAP.get(resourceTypeText);
        }

        Optional<MCRLabel> label = Optional.empty();
        try {
            MCRCategory category = MCRCategoryDAOFactory
                .obtainInstance()
                .getCategory(MCRCategoryID.ofString("typeOfResource:" + resourceTypeText), 0);
            label = category.getLabel("x-mapping-his-pub-resource-value");
        } catch (Exception ex) {
            LOGGER.warn("Could not resolve label x-mapping-his-pub-resource-value for category {}",
                ("typeOfResource:" + resourceTypeText), ex);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationResourceValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationResourceValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationResourceValue> availableResourceTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationResourceValue>>() {
                });

            // find his key of resourceType
            if (label.isPresent()) {
                String text = label.get().getText();
                Optional<SysValue.PublicationResourceValue> resolved = availableResourceTypes
                    .stream()
                    .filter(t -> text.equals(t.getDefaultText()))
                    .findFirst();

                if (resolved.isPresent()) {
                    return resolved.get();
                }
            }

            // return default his resource type
            return availableResourceTypes
                .stream()
                .filter(t -> "Sonstige Darstellungsform".equals(t.getDefaultText()))
                .findFirst().get();
        }
    }

    protected SysValue createParent(String mcrid) {
        LOGGER.info("Creating {} as parent is required", mcrid);
        return HISinOneCommands.publish(mcrid);
    }

    /**
     * Resolves the {@link SysValue} with id in HISinOne of the given mycoreobject id.
     *
     * @param mcrid the mcrid of a publication
     *
     * @return a {@link SysValue} with for the publication of the given mycoreobject id or {@link SysValue#UnresolvedSysValue}
     */
    private SysValue resolvePublication(String mcrid) {
        if (!exists(mcrid)) {
            return SysValue.UnresolvedSysValue;
        }

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
        if (mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).size() == 0) {
            return SysValue.UnresolvedSysValue;
        }

        String hisid = mcrObject.getService().getFlags(HISInOneServiceFlag.getName()).get(0);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.Publication.class) + "/" + hisid)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.Publication.class));
                return SysValue.ErroneousSysValue;
            }

            SysValue.Publication publication = response.readEntity(SysValue.Publication.class);
            return publication;
        }
    }

    /**
     * Resolves the his-id by the given peerreviewed category id.
     *
     * @param peerReviewedCategId the categ id category 'peerreviewed';
     *
     * @return {@link SysValue}
     * */
    private SysValue resolvePeerReviewedType(String peerReviewedCategId) {
        if (PEER_REVIEWED_TYPE_MAP.containsKey(peerReviewedCategId)) {
            return PEER_REVIEWED_TYPE_MAP.get(peerReviewedCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PeerReviewedValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PeerReviewedValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PeerReviewedValue> prTypes = response.readEntity(
                new GenericType<List<SysValue.PeerReviewedValue>>() {
                });

            var text = switch (peerReviewedCategId) {
                case "true" -> "ja";
                case "false" -> "nein";
                default -> "keine Angabe";
            };

            Optional<SysValue.PeerReviewedValue> peerReviewedValue = prTypes
                .stream()
                .filter(t -> text.equals(t.getUniqueName()))
                .findFirst();
            if (peerReviewedValue.isPresent()) {
                PEER_REVIEWED_TYPE_MAP.put(peerReviewedCategId, peerReviewedValue.get());
                return peerReviewedValue.get();
            }
        }

        return SysValue.UnresolvedSysValue;
    }

    /**
     * Resolves the his-id by the given research area id.
     *
     * @param accessRightsCategId the id of the category Access Rights - KDSF
     *
     * @return {@link SysValue}
     * */
    private SysValue resolvePublicationAccessType(String accessRightsCategId) {
        if (PUBLICATION_ACCESS_TYPE_MAP.containsKey(accessRightsCategId)) {
            return PUBLICATION_ACCESS_TYPE_MAP.get(accessRightsCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationAccessTypeValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationAccessTypeValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationAccessTypeValue> accessTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationAccessTypeValue>>() {
                });

            MCRCategoryID mcrCategoryID = MCRCategoryID.ofString("accessrights:" + accessRightsCategId);
            MCRCategory mcrCategory = MCRCategoryDAOFactory.obtainInstance().getCategory(mcrCategoryID, 1);

            Optional<MCRLabel> label = mcrCategory.getLabel("en");

            if (!label.isPresent()) {
                return SysValue.UnresolvedSysValue;
            }

            String text = label.get().getText().toLowerCase(Locale.ROOT);
            Optional<SysValue.PublicationAccessTypeValue> first = accessTypes
                .stream()
                .filter(patv -> text.equals(patv.getDefaultText().toLowerCase(Locale.ROOT)))
                .findFirst();

            if (first.isEmpty()) {
                return SysValue.UnresolvedSysValue;
            }

            PUBLICATION_ACCESS_TYPE_MAP.put(accessRightsCategId, first.get());
            return first.get();
        }
    }

    /**
     * Resolves the his-id by the given research area id.
     *
     * @param areaCategId the id of the research area
     *
     * @return the id of that research area in HISinOne
     * */
    private SysValue resolveResearchAreaKdsf(String areaCategId) {
        if (RESEARCH_AREA_TYPE_MAP.containsKey(areaCategId)) {
            return RESEARCH_AREA_TYPE_MAP.get(areaCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.ResearchAreaKdsfValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.ResearchAreaKdsfValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.ResearchAreaKdsfValue> availableTypes = response.readEntity(
                new GenericType<List<SysValue.ResearchAreaKdsfValue>>() {
                });

            Optional<SysValue.ResearchAreaKdsfValue> raKdsfValue = availableTypes
                .stream()
                .filter(v -> areaCategId.equals(v.getUniqueName()))
                .findFirst();

            if (raKdsfValue.isPresent()) {
                RESEARCH_AREA_TYPE_MAP.put(areaCategId, raKdsfValue.get());
                return raKdsfValue.get();
            }
        }

        return SysValue.UnresolvedSysValue;
    }

    /**
     * Resolves the HISinOne id of the given identifier.
     *
     * @param identifierType the type of the identifier, like doi, urn, ...
     *
     * @return the SysValue containing the id of that identifier.
     * */
    private SysValue resolveIdentifierType(String identifierType) {
        if (IDENTIFIER_TYPE_MAP.containsKey(identifierType)) {
            return IDENTIFIER_TYPE_MAP.get(identifierType);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.GlobalIdentifierType.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.GlobalIdentifierType.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.GlobalIdentifierType> availableTypes = response.readEntity(
                new GenericType<List<SysValue.GlobalIdentifierType>>() {
                });

            Optional<SysValue.GlobalIdentifierType> type = availableTypes
                .stream()
                .filter(t -> identifierType.toUpperCase(Locale.ROOT).equals(t.getUniqueName().toUpperCase(Locale.ROOT)))
                .findFirst();

            if (type.isPresent()) {
                IDENTIFIER_TYPE_MAP.put(identifierType, type.get());
                return type.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    protected SysValue resolvePublicationType(String fromXpathMapping) {
        if (PUBLICATION_TYPE_MAP.containsKey(fromXpathMapping)) {
            return PUBLICATION_TYPE_MAP.get(fromXpathMapping);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationTypeValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationTypeValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationTypeValue> pubTypeValues = response.readEntity(
                new GenericType<List<SysValue.PublicationTypeValue>>() {
                });

            String expectedType = MCRXMLFunctions.getDisplayName("kdsfPublicationType", fromXpathMapping, "de");

            Optional<SysValue.PublicationTypeValue> tpv = pubTypeValues
                .stream()
                .filter(pubType -> pubType.getUniqueName().equals(expectedType))
                .findFirst();

            PUBLICATION_TYPE_MAP.put(fromXpathMapping, tpv.get());
            return tpv.get();
        }
    }

    /**
     * Determines the documentType on base of fromXpathMapping/publicationType. Is currently fixed to 'Bibliographie'
     * */
    private SysValue resolveDocumentType(String fromXpathMapping) {
        if (DOCUMENT_TYPE_MAP.containsKey(fromXpathMapping)) {
            return DOCUMENT_TYPE_MAP.get(fromXpathMapping);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response bookResp = hisClient.get(SysValue.resolve(SysValue.DocumentTypeBook.class));
            Response articleResp = hisClient.get(SysValue.resolve(SysValue.DocumentTypeArticle.class));) {

            if (bookResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(bookResp, SysValue.resolve(SysValue.DocumentTypeBook.class));
                return SysValue.ErroneousSysValue;
            }
            if (articleResp.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(articleResp, SysValue.resolve(SysValue.DocumentTypeArticle.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.DocumentType> ofBook = bookResp.readEntity(new GenericType<List<SysValue.DocumentType>>() {
            });
            ofBook.addAll(articleResp.readEntity(new GenericType<List<SysValue.DocumentType>>() {
            }));

            // remove duplicates
            List<SysValue.DocumentType> list = ofBook
                .stream()
                .collect(Collectors
                    .toMap(SysValue.DocumentType::getId, existing -> existing, (existing, replace) -> existing))
                .values()
                .stream()
                .toList();

            String documentTypeName = MCRXMLFunctions.getDisplayName("kdsfDocumentType", fromXpathMapping, "de");
            Optional<SysValue.DocumentType> documentType = list
                .stream()
                .filter(v -> v.getDefaultText().equals(documentTypeName))
                .findFirst();

            if (documentType.isPresent()) {
                DOCUMENT_TYPE_MAP.put(fromXpathMapping, documentType.get());
                return documentType.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    private SysValue resolveThesisType(String ubogenre) {
        if (THESIS_TYPE_MAP.containsKey(ubogenre)) {
            return THESIS_TYPE_MAP.get(ubogenre);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.QualificationThesisValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.QualificationThesisValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.QualificationThesisValue> thesisValues = response.readEntity(
                new GenericType<List<SysValue.QualificationThesisValue>>() {
                });

            MCRCategoryID categId = MCRCategoryID.ofString("ubogenre:" + ubogenre);
            MCRCategoryID thesisCategId = MCRCategoryID.ofString("ubogenre:thesis");
            List<MCRCategory> children = MCRCategoryDAOFactory.obtainInstance().getChildren(thesisCategId);
            boolean isThesis = children.stream().filter(c -> c.getId().equals(categId)).findAny().isPresent();

            SysValue.QualificationThesisValue sysValue = null;
            if (isThesis) {
                String text = MCRCategoryDAOFactory
                    .obtainInstance()
                    .getCategory(categId, -1).getLabel("de").get()
                    .getText();

                Optional<SysValue.QualificationThesisValue> qtv = thesisValues.stream()
                    .filter(tv -> text.equals(tv.getDefaultText())).findFirst();

                if (qtv.isPresent()) {
                    sysValue = qtv.get();
                }
            } else {
                sysValue = thesisValues.stream()
                    .filter(tv -> "nicht zutreffend".equals(tv.getDefaultText())).findFirst().get();
            }
            THESIS_TYPE_MAP.put(ubogenre, sysValue);
            return sysValue;
        }
    }

    /**
     * Resolves destatis class. If you want the default value {@code ohne Angabe} invoke uri resolver like so:
     * {@code hisinone:resolve:id:subjectArea}
     *
     * @param destatisId the destatis id to resolve
     * */
    private SysValue resolveSubjectArea(String destatisId) {
        if (SUBJECT_AREA_TYPE_MAP.containsKey(destatisId)) {
            return SUBJECT_AREA_TYPE_MAP.get(destatisId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.SubjectAreaValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.SubjectAreaValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.SubjectAreaValue> subjectAreas = response.readEntity(
                new GenericType<List<SysValue.SubjectAreaValue>>() {
                });

            Optional<SysValue.SubjectAreaValue> areaValue = subjectAreas.stream()
                .filter(subjectAreaValue -> destatisId.equals(subjectAreaValue.getUniqueName()))
                .findFirst();

            if (areaValue.isPresent()) {
                SUBJECT_AREA_TYPE_MAP.put(destatisId, areaValue.get());
                return areaValue.get();
            }

            // default value ('ohne Angabe')
            areaValue = subjectAreas.stream()
                .filter(subjectAreaValue -> "OA".equalsIgnoreCase(subjectAreaValue.getUniqueName()))
                .findFirst();

            return areaValue.isPresent() ? areaValue.get() : SysValue.UnresolvedSysValue;
        }
    }

    /**
     * Supported values are:
     * <pre>
     *  Autor/-in
     *  Herausgeber/-in
     *  Körperschaft mit Autorenfunktion
     *  Körperschaft mit Herausgeberfunktion
     *  Gruppe mit Autorenfunktion
     *  Gruppe mit Herausgeberfunktion
     * </pre>
     * */
    protected SysValue resolveCreatorType(String value) {
        String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

        if (CREATOR_TYPE_MAP.containsKey(decodedValue)) {
            return CREATOR_TYPE_MAP.get(decodedValue);
        }

        String path = SysValue.resolve(SysValue.PublicationCreatorTypeValue.class);
        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(path)) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, path);
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationCreatorTypeValue> creatorTypes = response.readEntity(
                new GenericType<List<SysValue.PublicationCreatorTypeValue>>() {
                });

            Optional<SysValue.PublicationCreatorTypeValue> creatorTypeValue = creatorTypes
                .stream()
                .filter(state -> decodedValue.equals(state.getDefaultText()))
                .findFirst();

            if (creatorTypeValue.isPresent()) {
                CREATOR_TYPE_MAP.put(decodedValue, creatorTypeValue.get());
                return creatorTypeValue.get();
            }

            return SysValue.UnresolvedSysValue;
        }
    }

    /**
     * Resolves the HISinOne visibiltyValue by the current hsb publication status.
     *
     * @param statusCategId the current status category id of the publication
     *
     * @return the {@link SysValue}
     * */
    protected SysValue resolveVisibility(String statusCategId) {
        if (VISIBILITY_TYPE_MAP.containsKey(statusCategId)) {
            return VISIBILITY_TYPE_MAP.get(statusCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.VisibilityValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.VisibilityValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.VisibilityValue> visState = response.readEntity(
                new GenericType<List<SysValue.VisibilityValue>>() {
                });

            var id = switch (statusCategId) {
                case "confirmed", "unchecked" ->
                    visState.stream().filter(state -> "public".equals(state.getUniqueName())).findFirst().get();
                default -> visState.stream().filter(state -> "hidden".equals(state.getUniqueName())).findFirst()
                    .get();
            };

            VISIBILITY_TYPE_MAP.put(statusCategId, id);
            return id;
        }
    }

    protected SysValue resolveLicense(String licenseCategId) {
        if (licenseCategId == null || licenseCategId.isEmpty()) {
            return SysValue.UnresolvedSysValue;
        }

        if (LICENSE_TYPE_MAP.containsKey(licenseCategId)) {
            return LICENSE_TYPE_MAP.get(licenseCategId);
        }

        String displayName = MCRXMLFunctions.getDisplayName("licenses", licenseCategId, "de");

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.LicenseValue.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.LicenseValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.LicenseValue> licenseValues = response.readEntity(
                new GenericType<List<SysValue.LicenseValue>>() {
                });

            Optional<SysValue.LicenseValue> id = licenseValues.stream()
                .filter(licenseValue -> displayName.equals(licenseValue.getShortText()))
                .findFirst();

            if (id.isPresent()) {
                LICENSE_TYPE_MAP.put(licenseCategId, id.get());
                return id.get();
            }
            return SysValue.UnresolvedSysValue;
        }
    }

    /**
     * Resolves the HISinOne {@link SysValue.PublicationState} by the current hsb publication status.
     *
     * @param statusCategId the current status category id of the publication
     *
     * @return the {@link SysValue}
     * */
    protected SysValue resolveState(String statusCategId) {
        if (STATE_TYPE_MAP.containsKey(statusCategId)) {
            return STATE_TYPE_MAP.get(statusCategId);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.PublicationState.class))) {

            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.PublicationState.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.PublicationState> pubState = response.readEntity(
                new GenericType<List<SysValue.PublicationState>>() {
                });

            var id = switch (statusCategId) {
                case "confirmed", "unchecked" ->
                    pubState.stream().filter(state -> "validiert".equals(state.getUniqueName())).findFirst().get();
                case "review" ->
                    pubState.stream().filter(state -> "Dateneingabe".equals(state.getUniqueName())).findFirst().get();
                default ->
                    pubState.stream().filter(state -> "zur Validierung".equals(state.getUniqueName())).findFirst()
                        .get();
            };

            STATE_TYPE_MAP.put(statusCategId, id);
            return id;
        }
    }

    protected SysValue resolveLanguage(String rfc5646) {
        if (LANGUAGE_TYPE_MAP.containsKey(rfc5646)) {
            return LANGUAGE_TYPE_MAP.get(rfc5646);
        }

        try (HISInOneClient hisClient = HISinOneClientFactory.create();
            Response response = hisClient.get(SysValue.resolve(SysValue.LanguageValue.class))) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                logError(response, SysValue.resolve(SysValue.LanguageValue.class));
                return SysValue.ErroneousSysValue;
            }

            List<SysValue.LanguageValue> languageValues = response.readEntity(
                new GenericType<List<SysValue.LanguageValue>>() {
                });

            Optional<SysValue.LanguageValue> languageValue = languageValues
                .stream()
                .filter(lv -> lv.getIso6391().equals(rfc5646))
                .findFirst();

            if (languageValue.isPresent()) {
                LANGUAGE_TYPE_MAP.put(rfc5646, languageValue.get());
            }

            return languageValue.isPresent() ? languageValue.get() : SysValue.UnresolvedSysValue;
        }
    }

    /**
     * Retrieve the desired field value. If the value cannot be obtained {@link SysValue#getId()} will be returned.
     *
     * @param sysValue the SysValue
     * @param fieldName the field name
     *
     * @return the value of the requested field name or {@link SysValue#getId()}.
     */
    protected int getFieldValue(SysValue sysValue, String fieldName) {
        Class clazz = sysValue.getClass();
        Field field = null;

        /* lookup field in class hierarchy */
        while (field == null && clazz != null) {
            LOGGER.debug("Checking for field {} in {}", fieldName, clazz.getSimpleName());
            Optional<Field> f = Arrays.stream(clazz.getDeclaredFields())
                .filter(df -> df.getName().equals(fieldName)).findFirst();
            if (f.isPresent()) {
                field = f.get();
            } else {
                LOGGER.debug("Field {} could not be obtained from {}. Checking superclass {}", fieldName,
                    clazz.getSimpleName(), clazz.getSuperclass());
            }
            clazz = clazz.getSuperclass();
        }

        /* field is unresolved */
        if (field == null) {
            return SysValue.UnresolvedSysValue.getId();
        }

        /* field is resolved */
        field.setAccessible(true);
        try {
            return (int) field.get(sysValue);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
            return SysValue.ErroneousSysValue.getId();
        }
    }

    protected List<Integer> getFieldValues(List<SysValue> sysValues, String fieldName) {
        return sysValues.stream().map(sysValue -> getFieldValue(sysValue, fieldName)).toList();
    }

    /**
     * Checks for valid {@link MCRObjectID} and if object actually exists.
     *
     * @param mcrid the id to check
     *
     * @return true if an object exists, false otherwise
     * */
    protected boolean exists(String mcrid) {
        if (!MCRObjectID.isValid(mcrid)) {
            LOGGER.error("{} is not a valid {}", mcrid, MCRObjectID.class.getSimpleName());
            return false;
        }

        MCRObjectID id = MCRObjectID.getInstance(mcrid);
        if (!MCRMetadataManager.exists(id)) {
            LOGGER.warn("{} does not exist", mcrid);
            return false;
        }
        return true;
    }

    private long getStartDate(String dateRange) throws ParseException {
        if (dateRange.length() == 4) {
            return new SimpleDateFormat("yyyy").parse(dateRange).getTime();
        }

        return SDF.parse(dateRange.substring(0, dateRange.indexOf("-"))).getTime();
    }

    private long getEndDate(String dateRange) throws ParseException {
        if (dateRange.length() == 4) {
            return new SimpleDateFormat("yyyy").parse(dateRange).getTime();
        }

        String start = dateRange.substring(0, dateRange.indexOf("-") - 2);
        String end = dateRange.substring(dateRange.indexOf("-") + 1);

        return SDF.parse(start + end).getTime();
    }

    /**
     * Logs the response to the error log. Closes the {@link Response} object.
     *
     * @param response the response
     */
    protected void logError(Response response, String endpoint) {
        LOGGER.error("{}: {}", endpoint, response.readEntity(String.class));
    }

    protected void logResolvingResult(String href, String resolvedValue) {
        LOGGER.info("Resolved {} to {}", href, resolvedValue);
    }
}
