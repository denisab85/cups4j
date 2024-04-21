package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JacksonXmlRootElement(localName = "attribute-value")
public class AttributeValue {

    @JacksonXmlProperty(localName = "set-of-keyword")
    protected SetOfKeyword setOfKeyword;

    @JacksonXmlProperty(localName = "set-of-enum")
    protected SetOfEnum setOfEnum;

    @JacksonXmlProperty(isAttribute = true)
    protected String tag;

    @JacksonXmlProperty(isAttribute = true, localName = "tag-name")
    protected String tagName;

    @JacksonXmlProperty(isAttribute = true)
    protected String value;

    @JacksonXmlProperty(isAttribute = true)
    protected String description;

}
