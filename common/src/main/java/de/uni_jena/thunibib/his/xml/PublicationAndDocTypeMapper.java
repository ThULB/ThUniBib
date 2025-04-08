package de.uni_jena.thunibib.his.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p>
 * Maps the ubo genre to a type name (publication or document type) from HISinOne-RES.
 * </p>
 * <p>
 * The mapping is expected to be provided as {@link MCRLabel} attribute. For publication type mapping use <code>x-mapping-his-genre</code>
 * and for document type mapping use <code>x-mapping-his-docType</code>.
 * </p>
 *
 *  <p>
 *      <strong>Example:</strong>
 *  </p>
 *
 * <p>
 *  <code>
 *  &lt;label xml:lang="x-mapping-his-genre" text="journal:Journalartikel, default:Sonstiger Publikationstyp, standalone:Sonstiger Publikationstyp"/&gt;<br/>
 *  &lt;label xml:lang="x-mapping-his-docType" text="journal:Wissenschaftlicher Artikel, default:Sonstiger Dokumenttyp"/&gt;
 *  </code>
 * </p>
 *
 *  <p>
 *  The <code>x-mapping-his-docType</code> might be omitted as some publication types do not require a document type.
 *  </p>
 *
 * @author shermann (Silvio Hermann)
 *
 * */
public class PublicationAndDocTypeMapper {

    private static final Logger LOGGER = LogManager.getLogger(PublicationAndDocTypeMapper.class);

    private static final MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();

    /**
     * Determines the HISinOne publication type name by examining the genre and the host type.
     *
     * @param ubogenreId the genre of a publication
     * @param hostGenre the genre of the mods:relatedItem of a publication
     *
     * @return the HISinOne-RES type name
     *
     * @throw {@link RuntimeException} when a default mapping is missing in classification ubogenre
     *
     *  @deprecated Use matching via classification and xpath
     * */
    @Deprecated
    public static String getPublicationTypeName(String ubogenreId, String hostGenre) {
        return getTypeNameFromXLabel("x-mapping-his-genre", ubogenreId, hostGenre);
    }

    /**
     * Determines the HISinOne document type name by examining the genre and the host type.
     *
     * @param ubogenreId the genre of a publication
     * @param hostGenre the genre of the mods:relatedItem of a publication
     *
     * @return the HISinOne-RES document type name or <code>null</code>
     * */
    public static String getDocumentTypeName(String ubogenreId, String hostGenre) {
        try {
            return getTypeNameFromXLabel("x-mapping-his-docType", ubogenreId, hostGenre);
        } catch (RuntimeException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    /**
     * Returns the default publication type name for the given genre.
     *
     * @param ubogenreId the ubogenre to check
     *
     * @throw {@link RuntimeException} when a default mapping is missing in classification ubogenre
     * */
    public static String getDefaultPublicationTypeName(String ubogenreId) {
        return PublicationAndDocTypeMapper.getPublicationTypeName(ubogenreId, "default");
    }

    private static String getTypeNameFromXLabel(String xLabelName, String ubogenreId, String hostGenre) {
        MCRCategory genre = DAO.getCategory(MCRCategoryID.fromString("ubogenre:" + ubogenreId), 0);
        Optional<MCRLabel> label = genre.getLabel(xLabelName);

        if (label.isEmpty()) {
            LOGGER.warn("No '{}' for genre '{}' and related item of type '{}'", xLabelName, ubogenreId, hostGenre );
            return null;
        }

        // get default mapping
        if ("default".equals(hostGenre)) {
            Optional<String> mapping = getMapping(label.get(), "default");

            if (mapping.isEmpty()) {
                LOGGER.error("No default mapping for genre {}", ubogenreId);
            }
            return mapping.isPresent() ? mapping.get() : null;
        }

        Optional<String> name = getMapping(label.get(), hostGenre);
        return name.isPresent() ? name.get() : getDefaultTypeNameFromXLabel(xLabelName, ubogenreId);
    }

    private static String getDefaultTypeNameFromXLabel(String xLabelName, String ubogenreId) {
        return getTypeNameFromXLabel(xLabelName, ubogenreId, "default");
    }

    private static Optional<String> getMapping(MCRLabel label, String hostGenre) {
        Optional<String> defaultMapping = Arrays.stream(label.getText().split(","))
            .map(m -> m.trim())
            .filter(m -> m.startsWith(hostGenre + ":"))
            .map(m -> m.substring(m.indexOf(":") + 1))
            .findFirst();

        return !defaultMapping.isEmpty() ? defaultMapping : Optional.empty();
    }
}
