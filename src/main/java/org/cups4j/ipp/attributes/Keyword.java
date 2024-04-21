package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "keyword")
public class Keyword {

    @JacksonXmlProperty(isAttribute = true)
    protected String value;

    @JacksonXmlProperty(isAttribute = true)
    protected String description;

}
