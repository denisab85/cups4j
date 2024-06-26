package org.cups4j;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cups4j.ipp.attributes.AttributeGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Result of a print request
 */
public class PrintRequestResult {
    private final IppResult ippResult;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int jobId;
    private String resultCode = "";
    @Getter
    private String resultDescription = "";

    public PrintRequestResult(IppResult ippResult) {
        this.ippResult = ippResult;
        if ((ippResult == null) || isNullOrEmpty(ippResult.getHttpStatusResponse())) {
            return;
        }
        initializeFromHttpStatusResponse(ippResult);
        if (ippResult.getIppStatusResponse() != null) {
            initializeFromIppStatusResponse(ippResult);
        }
    }

    private void initializeFromIppStatusResponse(IppResult ippResult) {
        Pattern p = Pattern.compile("Status Code:(0x\\d+)(.*)");
        Matcher m = p.matcher(ippResult.getIppStatusResponse());
        if (m.find()) {
            this.resultCode = m.group(1);
            this.resultDescription = m.group(2);
        }
    }

    private void initializeFromHttpStatusResponse(IppResult ippResult) {
        Pattern p = Pattern.compile("HTTP/1.0 (\\d+) (.*)");
        Matcher m = p.matcher(ippResult.getHttpStatusResponse());
        if (m.find()) {
            this.resultCode = m.group(1);
            this.resultDescription = m.group(2);
        }
    }

    private boolean isNullOrEmpty(String string) {
        return isBlank(string);
    }

    public boolean isSuccessfulResult() {
        return resultCode != null && getResultCode().startsWith("0x00");
    }

    public String getResultCode() {
        if (ippResult.getHttpStatusCode() < 400) {
            return resultCode;
        } else {
            return "0x" + ippResult.getHttpStatusCode();
        }
    }

    public String getResultMessage() {
        if (ippResult.hasAttributeGroup("operation-attributes-tag")) {
            AttributeGroup attributeGroup = ippResult.getAttributeGroup("operation-attributes-tag");
            return attributeGroup.getAttributes("status-message").getValue();
        } else {
            return ippResult.getHttpStatusResponse();
        }
    }

}
