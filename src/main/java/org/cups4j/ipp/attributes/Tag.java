package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Root;

@Getter
@Setter
@Root(name = "tag")
public class Tag {

    @org.simpleframework.xml.Attribute(required = true)
    protected String value;
    @org.simpleframework.xml.Attribute(required = true)
    protected String name;
    @org.simpleframework.xml.Attribute(required = false)
    protected String description;
    @org.simpleframework.xml.Attribute(required = false)
    protected Short max;

}
