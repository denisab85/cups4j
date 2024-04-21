package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "attribute-list")
public class AttributeList {

    @JacksonXmlProperty(localName = "attribute-group")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<AttributeGroup> attributeGroups = new ArrayList<>();

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String schemaLocation;

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String description;

}
