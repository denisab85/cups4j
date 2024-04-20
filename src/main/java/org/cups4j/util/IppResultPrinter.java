package org.cups4j.util;

/**
 * Copyright (C) 2009 Harald Weyhing
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

import ch.ethz.vppserver.ippclient.IppResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.ipp.attributes.AttributeValue;

import java.util.List;

@Slf4j
@UtilityClass
public class IppResultPrinter {

    public static void print(IppResult result) {
        log.info(result.getHttpStatusResponse());
        log.info(result.getIppStatusResponse());
        List<AttributeGroup> attributeGroupList = result.getAttributeGroupList();
        printAttributeGroupList(attributeGroupList);
    }

    public static void print(IppResult result, boolean nurHeader) {
        if (nurHeader) {
            log.info(result.getHttpStatusResponse());
            log.info(result.getIppStatusResponse());
        } else {
            print(result);
        }
    }

    private static void printAttributeGroupList(List<AttributeGroup> list) {
        if (list != null) {
            for (AttributeGroup attributeGroup : list) {
                printAttributeGroup(attributeGroup);
            }
        }
    }

    private static void printAttributeGroup(AttributeGroup attributeGroup) {
        if (attributeGroup == null) {
            return;
        }
        log.info("\nAttribute Group: {}", attributeGroup.getTagName());
        List<Attribute> attributeList = attributeGroup.getAttributes();
        printAttributeList(attributeList);
    }

    private static void printAttributeList(List<Attribute> list) {
        if (list != null) {
            for (Attribute attr : list) {
                printAttribute(attr);
            }
        }
    }

    private static void printAttribute(Attribute attr) {
        if (attr == null) {
            return;
        }
        log.info("\tAttribute Name: {}", attr.getName());
        List<AttributeValue> attributeValueList = attr.getAttributeValues();
        printAttributeValueList(attributeValueList);
    }

    private static void printAttributeValueList(List<AttributeValue> list) {
        if (list != null) {
            for (AttributeValue attrValue : list) {
                log.info("\t\tAttribute Value: {}[{}] {}", attrValue.getTagName(), attrValue.getTag(), attrValue.getValue());
            }
        }
    }

}
