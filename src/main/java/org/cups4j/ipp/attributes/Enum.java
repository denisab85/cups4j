package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Getter
@Setter
@Root(name = "enum")
public class Enum {

    @Attribute
    protected String name;

    @Attribute
    protected String value;

    @Attribute(required = false)
    protected String description;

}
