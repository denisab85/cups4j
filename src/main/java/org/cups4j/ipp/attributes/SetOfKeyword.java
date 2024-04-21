package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@JacksonXmlRootElement(localName = "set-of-keyword")
public class SetOfKeyword {

    @JacksonXmlProperty(localName = "keyword")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected Set<Keyword> keywords = new LinkedHashSet<>();

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String description;

}
