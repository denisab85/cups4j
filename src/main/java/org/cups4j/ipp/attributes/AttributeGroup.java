package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Getter
@Root(name = "attribute-group")
public class AttributeGroup {

    @ElementList(entry = "attribute", inline = true)
    protected List<Attribute> attributes = new ArrayList<>();

    @Setter
    @org.simpleframework.xml.Attribute
    protected String tag;

    @Setter
    @org.simpleframework.xml.Attribute(name = "tag-name")
    protected String tagName;

    @Setter
    @org.simpleframework.xml.Attribute(required = false)
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
