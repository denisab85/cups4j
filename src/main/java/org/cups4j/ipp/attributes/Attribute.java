package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Getter
@Root(name = "attribute")
public class Attribute {

    @ElementList(entry = "attribute-value", inline = true)
    protected List<AttributeValue> attributeValues = new ArrayList<>();

    @Setter
    @org.simpleframework.xml.Attribute
    protected String name;

    @Setter
    @org.simpleframework.xml.Attribute(required = false)
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
