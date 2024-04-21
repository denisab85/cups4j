package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "tag-list")
public class TagList {

    @JacksonXmlProperty(localName = "tag")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<Tag> tags = new ArrayList<>();

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String schemaLocation;

}
