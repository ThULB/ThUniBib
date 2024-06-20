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
import de.uni_jena.thunibib.his.api.client.HISInOneClient;
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

import static org.eclipse.jetty.util.LazyList.addArray;
import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class PublicationHisResTransformer extends MCRToJSONTransformer {
    private static final Logger LOGGER = LogManager.getLogger(PublicationHisResTransformer.class);

    @Override
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
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, 'peerReviewedValue')]", xml, "peerReviewed");
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, 'publicationAccessTypeValue')]", xml, "access");
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, 'publicationCreatorTypeValue')]", xml, "publicationCreatorType");
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, 'visibilityValue')]", xml, "visibilityValue");
            addQualifiedObjectID(jsonObject, "//mods:classification[contains(@valueURI, 'state/publication')]", xml,"status");
            addQualifiedObjectID(jsonObject, "//mods:genre[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'publicationTypeValue')]", xml, "publicationType");
            addQualifiedObjectID(jsonObject, "//mods:genre[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'documentTypes')]", xml, "documentType");
            addQualifiedObjectID(jsonObject, "//mods:genre[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'qualificationThesisValue')]", xml, "qualificationThesis");

            addQualifiedObjectIDs(jsonObject, "//mods:classification[contains(@valueURI, 'researchAreaKdsfValue')]", xml, "researchAreasKdsf","id");
            addQualifiedObjectIDs(jsonObject, "//mods:classification[contains(@valueURI, 'subjectAreaValue')]", xml,"subjectAreas","id");
            addQualifiedObjectIDs(jsonObject, "//mods:language/mods:languageTerm[@authorityURI='" + HISInOneClient.HIS_IN_ONE_BASE_URL + "']", xml,"languages","id");
            addQualifiedObjectIDs(jsonObject, "//mods:subject[not(ancestor::mods:relatedItem)]/mods:topic", xml,"keyWords","defaulttext");

            addGlobalIdentifiers(jsonObject, xml);

            return jsonObject;
        } catch (JDOMException | SAXException e) {
            throw new IOException(
                "Could not generate JSON from " + source.getClass().getSimpleName() + ": " + source.getSystemId(), e);
        }
    }

    private void addGlobalIdentifiers(JsonObject jsonObject, Document xml) {
        JsonArray globalIdentifiers = new JsonArray();

        XPATH_FACTORY.compile("//mods:identifier[contains(@typeURI, '" + HISInOneClient.HIS_IN_ONE_BASE_URL + "')]",
                Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml).forEach(identifier -> {
                String typeURI = identifier.getAttributeValue("typeURI");
                int hisKey = Integer.parseInt(typeURI.substring(typeURI.lastIndexOf("#") + 1));

                JsonObject globalIdentifierType = new JsonObject();
                globalIdentifierType.addProperty("id", hisKey);

                JsonObject i = new JsonObject();
                i.addProperty("identifier", identifier.getText());
                i.add("globalIdentifierType", globalIdentifierType);

                globalIdentifiers.add(i);
            });

        if (!globalIdentifiers.isEmpty()) {
            jsonObject.add("globalIdentifiers", globalIdentifiers);
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

        if (!creators.isEmpty()) {
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
     * @param pName the name of the property
     * @param kName
     *
     *  <pre>
     *     "pName": [
     *     {
     *       "kName": 5
     *     },
     *     {
     *      "kName": 6
     *     }, ...]
     *  </pre>
     * */
    private void addQualifiedObjectIDs(JsonObject jsonObject, String xpath, Document xml, String pName, String kName) {
        final JsonArray jsonArray = new JsonArray();

        XPATH_FACTORY.compile(xpath, Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml)
            .forEach(text -> {
                JsonObject item = new JsonObject();
                try {
                    if (text.getText().matches("[+-]?\\d+")) {
                        item.addProperty(kName, Integer.parseInt(text.getText()));
                    } else {
                        item.addProperty(kName, text.getText());
                    }
                    jsonArray.add(item);
                } catch (NumberFormatException e) {
                    LOGGER.error(e);
                }
            });

        if (jsonArray.size() > 0) {
            jsonObject.add(pName, jsonArray);
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
                try {
                    documentType.addProperty("id", Integer.parseInt(genre.getText()));
                    jsonObject.add(propertyName, documentType);
                } catch (NumberFormatException e) {
                    LOGGER.error(e);
                }
            });
    }
}
