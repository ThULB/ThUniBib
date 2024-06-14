/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_jena.thunibib.his.content.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.uni_jena.thunibib.his.api.HISInOneClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRToJSONTransformer;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Optional;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class PublicationHisResTransformer extends MCRToJSONTransformer {
    private static final Logger LOGGER = LogManager.getLogger(PublicationHisResTransformer.class);

    protected JsonObject toJSON(MCRContent source) throws IOException {
        try {
            Document xml = source.asXML();
            JsonObject jsonObject = new JsonObject();

            addProperty(jsonObject, "//modsContainer/mods:mods/mods:abstract", xml, "textAbstract");
            addProperty(jsonObject, "//modsContainer/mods:mods/mods:originInfo/mods:dateIssued[1]", xml, "releaseYear");
            addProperty(jsonObject, "//modsContainer/mods:mods/mods:originInfo/mods:edition", xml, "edition");
            addProperty(jsonObject, "//modsContainer/mods:mods/mods:originInfo/mods:publisher", xml, "publisher");
            addProperty(jsonObject, "//modsContainer/mods:mods/mods:titleInfo/mods:subTitle", xml, "subtitle");
            addProperty(jsonObject, "//modsContainer/mods:mods/mods:titleInfo/mods:title", xml, "title");
            addProperty(jsonObject, "//modsContainer/mods:physicalDescription/mods:extent", xml, "numberOfPages");

            addCreators(jsonObject, xml);
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, '/cs/sys/values/visibilityValue')]",  xml, "visibilityValue");
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, '/fs/res/state/publication')]",  xml, "status");
            addQualifiedObjectID(jsonObject, "//mods:genre[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "']", xml, "publicationType");
            addQualifiedObjectIDs(jsonObject,"//mods:language/mods:languageTerm[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "']", xml,"languages");

            return jsonObject;
        } catch (JDOMException | SAXException e) {
            throw new IOException(
                "Could not generate JSON from " + source.getClass().getSimpleName() + ": " + source.getSystemId(), e);
        }
    }

    private void addCreators(JsonObject jsonObject, Document xml) {
        final JsonArray creators = new JsonArray();

        XPATH_FACTORY.compile("//mods:mods/mods:name[@type='personal']", Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml)
            .forEach(nameElement -> {
                final JsonObject name = new JsonObject();

                /* id of person in HISinOne */
                name.addProperty("id", "TODO-GET-ID-FROM-HIS");

                /* nameParts */
                nameElement
                    .getChildren("namePart", MODS_NAMESPACE)
                    .forEach(namePart -> {
                        var typeOfName = switch (namePart.getAttributeValue("type")) {
                            case "given" -> "firstname";
                            case "family" -> "creatorname";
                            default -> "unknown";
                        };
                        name.addProperty(typeOfName, namePart.getText());
                    });

                /* nameIdentifiers */
                final JsonArray personIdentifiers = new JsonArray();
                nameElement.
                    getChildren("nameIdentifier", MODS_NAMESPACE)
                    .stream()
                    .filter(identifierElement -> identifierElement.getAttributeValue("type") != null)
                    .forEach(identifierElement -> {
                        JsonObject identifier = new JsonObject();
                        JsonObject identifierType = new JsonObject();

                        identifier.addProperty("identifier", identifierElement.getText());
                        identifier.add("identifierType", identifierType);

                        identifierType.addProperty("id", identifierElement.getAttributeValue("type"));
                        personIdentifiers.add(identifier);
                    });
                name.add("personIdentifiers", personIdentifiers);

                /* affiliations */
                nameElement.getChildren("affiliation", MODS_NAMESPACE)
                    .stream()
                    .findFirst()
                    .ifPresent(affElement -> {
                        JsonArray affiliations = new JsonArray();
                        JsonObject affiliation = new JsonObject();
                        affiliation.addProperty("defaulttext", affElement.getText());
                        affiliations.add(affiliation);
                        name.add("affiliations", affiliations);
                    });
                creators.add(name);
            });

        if (creators.size() > 0) {
            jsonObject.add("creators", creators);
        }
    }

    private void addProperty(JsonObject jsonObject, String xpath, Document xml, String name) {
        XPATH_FACTORY.compile(xpath, Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml)
            .forEach(e -> jsonObject.addProperty(name, e.getText()));
    }

    /**
     * @param jsonObject the json object
     * @param xpath the xpath
     * @param xml the xml
     * @param propertyName the name of the property
     *
     *  <pre>
     *     "propertyName": [
     *     {
     *       "id": 5
     *     },
     *     {
     *      "id
     *     }, ...]
     *  </pre>
     * */
    private void addQualifiedObjectIDs(JsonObject jsonObject, String xpath, Document xml, String propertyName) {
        final JsonArray jsonArray = new JsonArray();

        XPATH_FACTORY.compile(xpath, Filters.element(), null, MODS_NAMESPACE).evaluate(xml)
            .forEach(text -> {
                try {
                    JsonObject item = new JsonObject();
                    item.addProperty("id", Integer.parseInt(text.getText()));
                    jsonArray.add(item);
                } catch (NumberFormatException e) {
                    LOGGER.error(e);
                }
            });

        if (jsonArray.size() > 0) {
            jsonObject.add(propertyName, jsonArray);
        }
    }

    /**
     * @param jsonObject the json object
     * @param xpath the xpath
     * @param xml the xml
     * @param propertyName the name of the property
     *
     *  <pre>
     *     "propertyName": {
     *     "id": integer value determined by xpath
     *   }
     *  </pre>
     * */
    private void addQualifiedObjectID(JsonObject jsonObject, String xpath, Document xml, String propertyName) {
        Optional.ofNullable(
                XPATH_FACTORY.compile(xpath, Filters.element(), null, MODS_NAMESPACE).evaluateFirst(xml))
            .ifPresent(genre -> {
                JsonObject documentType = new JsonObject();
                documentType.addProperty("id", Integer.parseInt(genre.getText()));
                jsonObject.add(propertyName, documentType);
            });
    }
}
