package org.cups4j.ipp.attributes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "attribute-group")
public class AttributeGroup {

    @JacksonXmlProperty(localName = "attribute")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<Attribute> attributes = new ArrayList<>();

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String tag;

    @Setter
    @JacksonXmlProperty(isAttribute = true, localName = "tag-name")
    protected String tagName;

    @Setter
    @JacksonXmlProperty(isAttribute = true)
    protected String description;

    /**
     * Gets the attribute with the given name.
     *
     * @param name name of the attribute
     * @return attribute with the given name
     */
    public Attribute getAttributes(String name) {
        return getAttributes().stream()
                .filter(attr -> name.equals(attr.getName()))
                .findFirst()
                .orElse(new Attribute());
    }

}
