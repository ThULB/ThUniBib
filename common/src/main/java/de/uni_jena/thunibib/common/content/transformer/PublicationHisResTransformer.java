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

package de.uni_jena.thunibib.common.content.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRToJSONTransformer;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;
import static org.mycore.common.MCRConstants.XPATH_FACTORY;

public class PublicationHisResTransformer extends MCRToJSONTransformer {

    protected JsonObject toJSON(MCRContent source) throws IOException {
        try {
            Document xml = source.asXML();
            JsonObject jsonObject = new JsonObject();

            add(jsonObject, "//mods:mods/mods:titleInfo/mods:title", xml, "title");
            add(jsonObject, "//mods:mods/mods:titleInfo/mods:subTitle", xml, "subtitle");
            add(jsonObject, "//mods:mods/mods:originInfo/mods:edition", xml, "edition");
            add(jsonObject, "//mods:mods/mods:originInfo/mods:dateIssued[1]", xml, "releaseYear");
            add(jsonObject, "//mods:mods/mods:originInfo/mods:publisher", xml, "publisher");
            add(jsonObject, "//mods:mods/mods:abstract", xml, "textAbstract");
            addCreators(jsonObject, xml);

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

    private void add(JsonObject jsonObject, String xpath, Document xml, String name) {
        XPATH_FACTORY.compile(xpath, Filters.element(), null, MODS_NAMESPACE)
            .evaluate(xml)
            .forEach(e -> jsonObject.addProperty(name, e.getText()));
    }
}