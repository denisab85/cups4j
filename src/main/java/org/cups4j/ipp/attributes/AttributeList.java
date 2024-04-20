//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.14 at 12:03:17 PM MESZ 
//

package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "attribute-list")
public class AttributeList {
    @Attribute
    protected String schemaLocation;

    @ElementList(entry = "attribute-group", inline = true, required = true)
    protected List<AttributeGroup> attributeGroup;

    @Setter
    @Getter
    @org.simpleframework.xml.Attribute(required = false)
    protected String description;

    /**
     * Gets the value of the attributeGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the attributeGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getAttributeGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AttributeGroup }
     */
    public List<AttributeGroup> getAttributeGroup() {
        if (attributeGroup == null) {
            attributeGroup = new ArrayList<AttributeGroup>();
        }
        return this.attributeGroup;
    }

}
