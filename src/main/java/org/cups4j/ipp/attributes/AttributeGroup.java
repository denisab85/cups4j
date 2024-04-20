//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.14 at 12:03:17 PM MESZ 
//

package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "attribute-group")
public class AttributeGroup {
    @ElementList(entry = "attribute", inline = true)
    protected List<Attribute> attribute;

    @Setter
    @Getter
    @org.simpleframework.xml.Attribute
    protected String tag;

    @Setter
    @Getter
    @org.simpleframework.xml.Attribute(name = "tag-name")
    protected String tagName;

    @Setter
    @Getter
    @org.simpleframework.xml.Attribute(required = false)
    protected String description;

    /**
     * Gets the value of the attribute property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the attribute property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getAttribute().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Attribute }
     */
    public List<Attribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<>();
        }
        return this.attribute;
    }

    /**
     * Gets the attribute with the given name.
     *
     * @param name name of the attribute
     * @return attribute with the given name
     */
    public Attribute getAttribute(String name) {
        for (Attribute attr : getAttribute()) {
            if (name.equals(attr.getName())) {
                return attr;
            }
        }
        return new Attribute();
    }

}
