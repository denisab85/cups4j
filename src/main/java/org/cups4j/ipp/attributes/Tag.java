package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Getter
@Setter
@Root(name = "tag")
public class Tag {

    @Attribute
    protected String value;

    @Attribute
    protected String name;

    @Attribute(required = false)
    protected String description;

    @Attribute(required = false)
    protected Short max;

}
