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

@Getter
@Root(name = "attribute-list")
public class AttributeList {

    @ElementList(entry = "attribute-group", inline = true)
    protected List<AttributeGroup> attributeGroups = new ArrayList<>();

    @Setter
    @Attribute
    protected String schemaLocation;

    @Setter
    @Attribute(required = false)
    protected String description;

}
