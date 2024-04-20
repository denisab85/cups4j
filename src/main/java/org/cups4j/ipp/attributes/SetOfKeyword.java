package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Root(name = "set-of-keyword")
public class SetOfKeyword {

    @ElementList(entry = "keyword", inline = true)
    protected Set<Keyword> keywords = new LinkedHashSet<>();

    @Setter
    @Attribute(required = false)
    protected String description;

}
