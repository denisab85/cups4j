package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Getter
@Setter
@Root(name = "keyword")
public class Keyword {

    @Attribute
    protected String value;

    @Attribute(required = false)
    protected String description;

}
