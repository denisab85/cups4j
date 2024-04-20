package org.cups4j.ipp.attributes;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "tag-list")
public class TagList {
    @Setter
    @Getter
    @Attribute
    protected String schemaLocation;

    @ElementList(entry = "tag", inline = true)
    protected List<Tag> tag;

    public List<Tag> getTag() {
        if (tag == null) {
            tag = new ArrayList<>();
        }
        return this.tag;
    }

}
