package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Getter
@Root(name = "tag-list")
public class TagList {

    @ElementList(entry = "tag", inline = true)
    protected List<Tag> tags = new ArrayList<>();

    @Setter
    @Attribute
    protected String schemaLocation;

}
