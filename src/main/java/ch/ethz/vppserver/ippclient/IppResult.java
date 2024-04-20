package ch.ethz.vppserver.ippclient;

import lombok.Getter;
import lombok.Setter;
import org.cups4j.ipp.attributes.AttributeGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
@Setter
@Getter
public class IppResult {

    private String httpStatusResponse = null;
    private String ippStatusResponse = null;
    private List<AttributeGroup> attributeGroupList = new ArrayList<>();
    private int httpStatusCode;

    public AttributeGroup getAttributeGroup(String tagName) {
        for (AttributeGroup group : attributeGroupList) {
            if (tagName.equalsIgnoreCase(group.getTagName())) {
                return group;
            }
        }
        throw new IllegalArgumentException("tag '" + tagName + "' not found in " + attributeGroupList
                + "; Http status response: " + httpStatusResponse + ", Ipp status response: " + ippStatusResponse);
    }

    public boolean hasAttributeGroup(String tagName) {
        for (AttributeGroup group : attributeGroupList) {
            if (tagName.equalsIgnoreCase(group.getTagName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrintQueueUnavailable() {
        return ippStatusResponse != null && ippStatusResponse.contains("client-error-not-possible");
    }

}
