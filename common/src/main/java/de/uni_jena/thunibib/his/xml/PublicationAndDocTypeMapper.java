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
     * */
    public static String getPublicationTypeName(String ubogenreId, String hostGenre) {
        return getPublicationTypeName("x-mapping-his-genre", ubogenreId, hostGenre);
    }

    private static String getPublicationTypeName(String xLabelName, String ubogenreId, String hostGenre) {
        MCRCategory genre = DAO.getCategory(MCRCategoryID.fromString("ubogenre:" + ubogenreId), 0);
        MCRLabel label = genre.getLabel(xLabelName).orElseThrow(() -> new RuntimeException(
            "No '" + xLabelName + "' for genre '" + ubogenreId + "' and related item of type '" + hostGenre + "'"));

        // get default mapping
        if ("none".equals(hostGenre)) {
            return getMapping(label, "default")
                .orElseThrow(() -> new RuntimeException("No default mapping for genre " + ubogenreId));
        }

        Optional<String> name = getMapping(label, hostGenre);
        return name.isPresent() ? name.get() : getDefaultPublicationTypeName(ubogenreId);
    }

    /**
     * Returns the default publication type name for the given genre.
     *
     * @param ubogenreId the ubogenre to check
     * */
    public static String getDefaultPublicationTypeName(String ubogenreId) {
        return PublicationAndDocTypeMapper.getPublicationTypeName(ubogenreId, "none");
    }

    private static Optional<String> getMapping(MCRLabel label, String hostGenre) {
        Optional<String> defaultMapping = Arrays.stream(label.getText().split(","))
            .map(m -> m.trim())
            .filter(m -> m.startsWith(hostGenre + ":"))
            .map(m -> m.substring(m.indexOf(":") + 1))
            .findFirst();

        return !defaultMapping.isEmpty() ? defaultMapping : Optional.empty();
    }

    public static String getDocumentTypeName(String ubogenreId) {
        return "Bibliographie";
    }
}
