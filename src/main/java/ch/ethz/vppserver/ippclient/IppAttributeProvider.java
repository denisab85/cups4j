package ch.ethz.vppserver.ippclient;

/**
 * Copyright (C) 2012 Harald Weyhing
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http://www.gnu.org/licenses/>.
 */

import lombok.Getter;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.ipp.attributes.AttributeList;
import org.cups4j.ipp.attributes.Tag;
import org.cups4j.ipp.attributes.TagList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Getter
public class IppAttributeProvider implements IIppAttributeProvider {

    private static final IppAttributeProvider INSTANCE = new IppAttributeProvider();

    private List<Tag> tagList = new ArrayList<Tag>();

    private List<AttributeGroup> attributeGroupList = new ArrayList<AttributeGroup>();

    private IppAttributeProvider() {
        try {
            InputStream tagListStream = IIppAttributeProvider.class.getClassLoader().getResourceAsStream(TAG_LIST_FILENAME);
            InputStream attListStream = IIppAttributeProvider.class.getClassLoader().getResourceAsStream(
                    ATTRIBUTE_LIST_FILENAME);

            Serializer serializer = new Persister();
            TagList tList = serializer.read(TagList.class, tagListStream);
            tagList = tList.getTag();

            AttributeList aList = serializer.read(AttributeList.class, attListStream);
            attributeGroupList = aList.getAttributeGroup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IppAttributeProvider getInstance() {
        return INSTANCE;
    }

}
