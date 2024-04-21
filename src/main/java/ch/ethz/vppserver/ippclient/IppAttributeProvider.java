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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.ipp.attributes.AttributeList;
import org.cups4j.ipp.attributes.Tag;
import org.cups4j.ipp.attributes.TagList;

import java.io.InputStream;
import java.util.List;

@Getter
public class IppAttributeProvider implements IIppAttributeProvider {

    private static final IppAttributeProvider INSTANCE = new IppAttributeProvider();

    private final List<Tag> tagList;

    private final List<AttributeGroup> attributeGroupList;

    private IppAttributeProvider() {
        try {
            InputStream tagListStream = IIppAttributeProvider.class.getClassLoader().getResourceAsStream(TAG_LIST_FILENAME);
            InputStream attListStream = IIppAttributeProvider.class.getClassLoader().getResourceAsStream(ATTRIBUTE_LIST_FILENAME);

            XmlMapper xmlMapper = new XmlMapper();
            TagList tList = xmlMapper.readValue(tagListStream, TagList.class);
            tagList = tList.getTags();

            AttributeList aList = xmlMapper.readValue(attListStream, AttributeList.class);
            attributeGroupList = aList.getAttributeGroups();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IppAttributeProvider getInstance() {
        return INSTANCE;
    }

}
