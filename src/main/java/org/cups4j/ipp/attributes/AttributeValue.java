package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Setter
@Getter
@Root(name = "attribute-value")
public class AttributeValue {

    @Element(name = "set-of-keyword", required = false)
    protected SetOfKeyword setOfKeyword;

    @Element(name = "set-of-enum", required = false)
    protected SetOfEnum setOfEnum;

    @Attribute
    protected String tag;

    @Attribute(name = "tag-name")
    protected String tagName;

    @Attribute(required = false)
    protected String value;

    @Attribute(required = false)
    protected String description;

}
