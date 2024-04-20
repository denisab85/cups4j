package org.cups4j.ipp.attributes;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AttributeGroupTest {

    private final AttributeGroup attributeGroup = new AttributeGroup();

    private static Attribute createAttribute(String name) {
        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setDescription("test of " + name);
        return attribute;
    }

    @Test
    public void testGetAttribute() {
        attributeGroup.attribute = new ArrayList<>();
        attributeGroup.attribute.add(createAttribute("hello"));
        attributeGroup.attribute.add(createAttribute("world"));
        assertEquals(2, attributeGroup.getAttribute().size());
    }

}