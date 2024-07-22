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
import de.uni_jena.thunibib.his.xml.HISInOneServiceFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRToJSONTransformer;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.uni_jena.thunibib.his.api.client.HISInOneClient.HIS_IN_ONE_BASE_URL;
import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class PublicationHisResTransformer extends MCRToJSONTransformer {
    private static final Logger LOGGER = LogManager.getLogger(PublicationHisResTransformer.class);

    protected static final String PARSEABLE_INT = "[+-]?\\d+";

    @Override
    protected JsonObject toJSON(MCRContent source) throws IOException {

        try {
            Document xml = source.asXML();
            JsonObject jsonObject = new JsonObject();

            addPropertyInt(jsonObject, "//servflag[@type='" + HISInOneServiceFlag.getName() + "']", xml, "id");
            addPropertyInt(jsonObject, "//servflag[@type='" + HISInOneServiceFlag.getName() + "-lockVersion']", xml, "lockVersion");

            addProperty(jsonObject, "//mods:mods/mods:abstract", xml, "textAbstract", true);
            addProperty(jsonObject, "//mods:mods/mods:originInfo/mods:dateIssued[1]", xml, "releaseYear", true);
            addProperty(jsonObject, "//mods:mods/mods:originInfo/mods:edition", xml, "edition", true);
            addProperty(jsonObject, "//mods:mods/mods:titleInfo/mods:subTitle", xml, "subtitle", true);
            addProperty(jsonObject, "//mods:mods/mods:titleInfo/mods:title", xml, "title", true);
            addProperty(jsonObject, "//mods:mods/mods:note[not(@type='intern')]", xml, "commentary", false);
            addProperty(jsonObject, "//mods:mods/mods:part/mods:detail[@type='volume']/mods:number", xml, "volume", true);

            addExtent(jsonObject, xml);
            addSampleCreator(jsonObject);
            /** TODO: Remove comments in production
             addCreators(jsonObject, xml);
             */
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'publicationResourceValue')]", xml,"publicationResource");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'publisher')]", xml,"publisher");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'peerReviewedValue')]", xml, "peerReviewedProcess");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'publicationAccessTypeValue')]", xml, "access");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'publicationCreatorTypeValue')]", xml,"publicationCreatorType");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'visibilityValue')]", xml, "visibilityValue");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:classification[contains(@valueURI, 'state/publication')]", xml, "status");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:genre[@authorityURI='" + HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'publicationTypeValue')]", xml, "publicationType");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:genre[@authorityURI='" + HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'documentTypes')]", xml, "documentType");
            addQualifiedObjectID(jsonObject, "//mods:mods/mods:genre[@authorityURI='" + HIS_IN_ONE_BASE_URL + "'][contains(@valueURI, 'qualificationThesisValue')]", xml, "qualificationThesis");

            addQualifiedObjectIDs(jsonObject,"//mods:mods/mods:classification[contains(@valueURI, 'researchAreaKdsfValue')]", xml,"researchAreasKdsf", "id");
            addQualifiedObjectIDs(jsonObject,"//mods:mods/mods:classification[contains(@valueURI, 'subjectAreaValue')]", xml, "subjectAreas", "id");
            addQualifiedObjectIDs(jsonObject,"//mods:mods/mods:language/mods:languageTerm[@authorityURI='" + HIS_IN_ONE_BASE_URL + "']", xml, "languages", "id");
            addQualifiedObjectIDs(jsonObject, "//mods:mods/mods:subject/mods:topic", xml, "keyWords", "defaulttext");

            addGlobalIdentifiers(jsonObject, xml);

            return jsonObject;
        } catch (JDOMException | SAXException e) {
            throw new IOException(
                "Could not generate JSON from " + source.getClass().getSimpleName() + ": " + source.getSystemId(), e);
        }
    }

    private void addExtent(JsonObject jsonObject, Document xml) {
        Optional
            .ofNullable(XPATH_FACTORY
                .compile("//modsContainer/mods:mods/mods:physicalDescription/mods:extent", Filters.element(), null,
                    MODS_NAMESPACE)
                .evaluateFirst(xml))
            .ifPresent(extent -> {
                String text = extent.getText().trim();
                if (text.matches(PublicationHisResTransformer.PARSEABLE_INT)) {
                    jsonObject.addProperty("numberOfPages", Integer.parseInt(extent.getText()));
                }
            });
    }

    private void addGlobalIdentifiers(JsonObject jsonObject, Document xml) {
        JsonArray globalIdentifiers = new JsonArray();

        XPATH_FACTORY.compile("//mods:identifier[contains(@typeURI, '" + HIS_IN_ONE_BASE_URL + "')]",
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

    /**
     * For Testing
     * */
    private void addSampleCreator(JsonObject jsonObject) {
        LOGGER.warn("{}#addSampleCreator invoked", PublicationHisResTransformer.class.getName());
        JsonArray creators = new JsonArray();
        JsonObject name = new JsonObject();
        name.addProperty("id", 135);
        name.addProperty("creatorname", "Krüger");
        name.addProperty("firstname", "Gudrun");

        creators.add(name);
        jsonObject.add("creators", creators);
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

    private void addProperty(JsonObject jsonObject, String xpath, Document xml, String pName, boolean single) {
        List<Element> list = XPATH_FACTORY
            .compile(xpath, Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml);

        if (single) {
            list.forEach(e -> jsonObject.addProperty(pName, e.getText()));
        } else {
            if (!list.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                list.forEach(e -> builder.append(e.getText() + "\n"));
                jsonObject.addProperty(pName, builder.toString().trim());
            }
        }
    }

    private void addPropertyInt(JsonObject jsonObject, String xpath, Document xml, String pName) {
        List<Element> list = XPATH_FACTORY
            .compile(xpath, Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml);
        list.forEach(e -> jsonObject.addProperty(pName, Integer.parseInt(e.getText())));
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
                    if (text.getText().matches(PublicationHisResTransformer.PARSEABLE_INT)) {
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
