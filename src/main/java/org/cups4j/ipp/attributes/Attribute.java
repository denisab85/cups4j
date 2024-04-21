package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "attribute")
public class Attribute {

    @JacksonXmlProperty(localName = "attribute-value")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<AttributeValue> attributeValues = new ArrayList<>();

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String name;

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String description;

    /**
     * Gets the attribute value as CSV string.
     *
     * @return all attribute values as CSV
     */
    public String getValue() {
        StringBuilder buf = new StringBuilder();
        for (AttributeValue av : getAttributeValues()) {
            buf.append(',');
            buf.append(av.getValue());
        }
        buf.append(' ');
        return buf.substring(1).trim();
    }

}
