package org.cups4j.ipp.attributes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AttributeGroupTest {

    private static Attribute createAttribute(String name) {
        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setDescription("test of " + name);
        return attribute;
    }

    @Test
    public void testGetAttribute() {
        AttributeGroup attributeGroup = new AttributeGroup();
        attributeGroup.attributes.add(createAttribute("hello"));
        attributeGroup.attributes.add(createAttribute("world"));
        assertEquals(2, attributeGroup.getAttributes().size());
    }

}