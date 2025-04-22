package de.uni_jena.thunibib.his.api.v1.cs.sys.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Class reflects values sent to and received from HISinOne.
 *
 * @author shermann (Silvio Hermann)
 * */
abstract public class SysValue {
    @JsonProperty("id")
    protected int id;
    @JsonProperty("lockVersion")
    private int lockVersion;
    @JsonProperty("objGuid")
    private String objGuid;
    @JsonProperty("shorttext")
    protected String shortText;
    @JsonProperty("defaulttext")
    protected String defaultText;
    @JsonProperty("longtext")
    protected String longText;
    @JsonProperty("text")
    protected String text;
    @JsonProperty("hiskeyId")
    protected int hisKeyId;
    @JsonProperty("sortorder")
    private int sortOrder;
    @JsonProperty("uniquename")
    private String uniqueName;

    private static final HashMap<Class<?>, String> SYSVALUE_TYPES = new HashMap<>();

    static {
        Arrays.stream(SysValue.class.getClasses())
            .filter(SysValue.class::isAssignableFrom)
            .filter(c -> c.isAnnotationPresent(HISinOnePath.class))
            .forEach(c -> {
                HISinOnePath annotation = c.getAnnotation(HISinOnePath.class);
                String path = annotation.path();
                SYSVALUE_TYPES.put(c, path);
            });
    }

    /**
     * This {@link SysValue} indicates an unresolved value in case a proper hisKeyId could not be obtained from HISinOne.
     * */
    @JsonIgnore
    static public final SysValue UnresolvedSysValue = new UnresolvedSysValue(-1);

    /**
     * This {@link SysValue} indicates an unresolved value in case an error occurred during the resolving of the key from
     * HISinOne.
     * */
    @JsonIgnore
    static public final SysValue ErroneousSysValue = new ErroneousSysValue(-2);
    @JsonIgnore
    static public final SysValue SuccessSysValue = new SuccessSysValue();

    public int getId() {
        return id;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public String getObjGuid() {
        return objGuid;
    }

    public String getShortText() {
        return shortText;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public String getLongText() {
        return longText;
    }

    public String getText() {
        return text;
    }

    public int getHisKeyId() {
        return hisKeyId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String toString() {
        return defaultText + ":" + id + ":lockVersion:" + lockVersion;
    }

    /**
     * Method resolves the REST-api endpoint for a given class extending {@link SysValue}.
     *
     * @param clazz the class to find the endpoint path for
     *
     * @return the path or null
     * */
    public static <T extends SysValue> String resolve(Class<T> clazz) {
        String resolvedPath = SYSVALUE_TYPES.get(clazz);
        return resolvedPath;
    }

    @HISinOnePath(path = "cs/sys/values/visibilityValue")
    static public class VisibilityValue extends SysValue {
    }

    /**
     * HISinOne 'Fachgebiete' classification == UBO 'fachreferate'/'destatis' classification
     * */
    @HISinOnePath(path = "cs/sys/values/subjectAreaValue")
    static public class SubjectAreaValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/researchAreaKdsfValue")
    static public class ResearchAreaKdsfValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/qualificationThesisValue")
    static public class QualificationThesisValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/publicationTypeValue")
    static public class PublicationTypeValue extends SysValue {
    }

    /**
     * Corresponds to UBO/HSB 'Type of resource' (Ressourcentyp).
     * */
    @HISinOnePath(path = "cs/sys/values/publicationResourceValue")
    static public class PublicationResourceValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/publicationCreatorTypeValue")
    static public class PublicationCreatorTypeValue extends SysValue {
    }

    /**
     * PublicationAccessType (Zugangsrecht nach KDSF)
     * */
    @HISinOnePath(path = "cs/sys/values/publicationAccessTypeValue")
    static public class PublicationAccessTypeValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/peerReviewedValue")
    static public class PeerReviewedValue extends SysValue {
    }

    @HISinOnePath(path = "cs/sys/values/languageValue")
    static public class LanguageValue extends SysValue {

        @JsonProperty("iso_639_2")
        String iso6392;
        @JsonProperty("iso_639_1")
        String iso6391;

        public String getIso6392() {
            return iso6392;
        }

        public String getIso6391() {
            return iso6391;
        }
    }

    /**
     * Endpoint for <strong>searching</strong> publishers.
     *
     * PublisherWrappedValue
     *
     * Path: <code>/api/v1/fs/res/wrapped/publisher?q=</code>
     * */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PublisherWrappedValue extends SysValue {
        public enum PathType {
            search, create
        }

        @JsonProperty("_foreigntext")
        String _foreigntext;

        @JsonProperty("place")
        String place;

        @JsonProperty("label")
        String label;

        @JsonProperty("language")
        LanguageValue language;

        public String getForeignText() {
            return _foreigntext;
        }

        public String getPlace() {
            return place;
        }

        public String getLabel() {
            return label;
        }
    }

    @HISinOnePath(path = "fs/res/wrapped/publisher")
    static public class PublisherWrappedValueSearch extends SysValue.PublisherWrappedValue {
    }

    @HISinOnePath(path = "fs/res/publisher")
    static public class PublisherWrappedValueCreate extends SysValue.PublisherWrappedValue {
    }

    static public class DocumentType extends SysValue {
    }

    @HISinOnePath(path = "fs/res/publication/documentTypes/article")
    static public class DocumentTypeArticle extends SysValue.DocumentType {
    }

    @HISinOnePath(path = "fs/res/publication/documentTypes/book")
    static public class DocumentTypeBook extends SysValue {
    }

    @HISinOnePath(path = "fs/res/state/publication")
    static public class PublicationState extends SysValue {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @HISinOnePath(path = "fs/res/publication")
    static public class Publication extends SysValue {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @HISinOnePath(path = "fs/res/journal")
    public class Journal extends SysValue {
    }

    @HISinOnePath(path = "fs/res/publication/globalIdentifierType")
    static public class GlobalIdentifierType extends SysValue {
    }

    /**
     * At least these rights: RIGHT_CS_PSV_PERSON_VIEW_PERSON_IDENTIFIER, RIGHT_CS_PSV_PERSON_VIEW_PERSON_MAINDATA
     * */
    @HISinOnePath(path = "cs/psv/person/identifier")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class PersonIdentifier extends SysValue {

        @JsonProperty("personId")
        private int personId;

        @JsonProperty("identifierValue")
        private String identifierValue;

        @JsonProperty("identifierType")
        private String identifierType;

        public static String getValueParameterName() {
            return "identifierValue";
        }

        public static String getTypeParameterName() {
            return "typeUniquename";
        }

        public int getPersonId() {
            return personId;
        }

        public String getIdentifierValue() {
            return identifierValue;
        }

        public String getIdentifierType() {
            return identifierType;
        }
    }

    @HISinOnePath(path = "fs/res/conference")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class Conference extends SysValue {
        @JsonProperty("city")
        private String city;

        public String getCity() {
            return city;
        }
    }

    private static class UnresolvedSysValue extends SysValue.DocumentType {
        public UnresolvedSysValue(int id) {
            this.id = id;
        }
    }

    private static class SuccessSysValue extends SysValue {
        public SuccessSysValue() {
            this.id = 1;
        }
    }

    private static class ErroneousSysValue extends SysValue {
        public ErroneousSysValue(int id) {
            this.id = id;
        }
    }
}
