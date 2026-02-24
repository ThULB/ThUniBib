package de.uni_jena.thunibib.his.so;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRConstants;

import java.util.HashMap;

/**
 * @author shermann (Silvio Hermann)
 * */
public class SOBuilder {

    private SOUnit root;

    private HashMap<Integer, SOUnit> unitMap;

    public SOBuilder(SOUnit root) {
        this.unitMap = new HashMap<>();
        this.root = root;
        this.unitMap.put(root.getlID(), root);
    }

    public SOUnit build() {
        unitMap.values().stream().filter(u -> u.getParentLiD() != -1).forEach(unit -> {
            int parentLiD = unit.getParentLiD();
            SOUnit parentUnit = unitMap.get(parentLiD);

            if (parentUnit != null) {
                parentUnit.addChild(unit);
            } else {
                root.addChild(unit);
            }
        });

        return root;
    }

    public void add(SOUnit toAdd) {
        unitMap.put(toAdd.getlID(), toAdd);
    }

    public Document toMCRClassification() {
        SOUnit build = build();
        Element mycoreclass = new Element("mycoreclass");
        mycoreclass.setAttribute("ID", String.valueOf(System.currentTimeMillis()));
        mycoreclass.setAttribute("noNamespaceSchemaLocation", "MCRClassification.xsd", MCRConstants.XSI_NAMESPACE);
        mycoreclass.addContent(
            new Element("label")
                .setAttribute("text", "Friedrich-Schiller-Universität Jena")
                .setAttribute("lang", "de", Namespace.XML_NAMESPACE));

        Element categories = new Element("categories");
        categories.addContent(build.toCategory());
        mycoreclass.addContent(categories);
        return new Document(mycoreclass);
    }
}
