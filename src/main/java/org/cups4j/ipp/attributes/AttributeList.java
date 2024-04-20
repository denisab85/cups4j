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
