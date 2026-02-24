package de.uni_jena.thunibib.his.so;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author shermann (Silvio Hermann)
 * */
public class SOUnit {
    private int lID;
    private int parentLiD;

    private HashMap<String, String> attributes;
    private List<SOUnit> children;

    public SOUnit(int lId, int parentId) {
        this.lID = lId;
        this.parentLiD = parentId;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
    }

    public int getParentLiD() {
        return this.parentLiD;
    }

    public int getlID() {
        return lID;
    }

    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public void addChild(SOUnit child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return attributes.get("defaulttext") + " [lid:" + this.lID + " plid: " + this.parentLiD + " children: "
            + this.children.size() + "]";
    }

    /**
     *
     * @param other the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof SOUnit)) {
            return false;
        }
        return this.lID == ((SOUnit) other).getlID();
    }

    public Element toCategory() {
        Element category = new Element("category");
        category.setAttribute("ID", Integer.toString(this.lID));
        Element label = new Element("label");
        label.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        label.setAttribute("text", this.attributes.get("defaulttext"));
        label.setAttribute("description", this.attributes.get("longtext"));
        category.addContent(label);

        this.children.forEach(child -> category.addContent(child.toCategory()));

        return category;
    }
}
