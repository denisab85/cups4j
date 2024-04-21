package org.cups4j.operations.ipp;

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

import ch.ethz.vppserver.ippclient.IppTag;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
public class IppPrintJobOperation extends IppOperation {

    public IppPrintJobOperation() {
        operationID = 0x0002;
        bufferSize = 8192;
    }

    public IppPrintJobOperation(int port) {
        this();
        this.ippPort = port;
    }

    /**
     * TODO: not all possibilities implemented
     *
     * @param ippBuf
     * @param attributeBlocks
     * @return
     * @throws UnsupportedEncodingException
     */
    protected static ByteBuffer getJobAttributes(ByteBuffer ippBuf, String[] attributeBlocks)
            throws UnsupportedEncodingException {
        if (ippBuf == null) {
            log.error("IppPrintJobOperation.getJobAttributes(): ippBuf is null");
            return null;
        }
        if (attributeBlocks == null) {
            return ippBuf;
        }

        ippBuf = IppTag.getJobAttributesTag(ippBuf);

        for (String attributeBlock : attributeBlocks) {
            String[] attr = attributeBlock.split(":");
            if (attr.length != 3) {
                return ippBuf;
            }
            String name = attr[0];
            String tagName = attr[1];
            String value = attr[2];

            switch (tagName) {
                case "boolean":
                    ippBuf = IppTag.getBoolean(ippBuf, name, value.equals("true"));
                    break;
                case "integer":
                    ippBuf = IppTag.getInteger(ippBuf, name, Integer.parseInt(value));
                    break;
                case "rangeOfInteger":
                    String[] range = value.split("-");
                    int low = Integer.parseInt(range[0]);
                    int high = Integer.parseInt(range[1]);
                    ippBuf = IppTag.getRangeOfInteger(ippBuf, name, low, high);
                    break;
                case "setOfRangeOfInteger":
                    String[] ranges = value.split(",");
                    for (String r : ranges) {
                        r = r.trim();
                        String[] values = r.split("-");

                        int value1 = Integer.parseInt(values[0]);
                        int value2 = value1;
                        // two values provided?
                        if (values.length == 2) {
                            value2 = Integer.parseInt(values[1]);
                        }

                        // first attribute value needs name, additional values need to get the "null" name
                        ippBuf = IppTag.getRangeOfInteger(ippBuf, name, value1, value2);
                        name = null;
                    }
                    break;
                case "keyword":
                    ippBuf = IppTag.getKeyword(ippBuf, name, value);
                    break;
                case "name":
                    ippBuf = IppTag.getNameWithoutLanguage(ippBuf, name, value);
                    break;
                case "enum":
                    ippBuf = IppTag.getEnum(ippBuf, name, Integer.parseInt(value));
                    break;
                case "resolution":
                    String[] resolution = value.split(",");
                    int value1 = Integer.parseInt(resolution[0]);
                    int value2 = Integer.parseInt(resolution[1]);
                    byte value3 = Byte.parseByte(resolution[2]);
                    ippBuf = IppTag.getResolution(ippBuf, name, value1, value2, value3);
                    break;
            }
        }
        return ippBuf;
    }

    /**
     * @param url printer-uri
     * @param map attributes
     *            i.e.job-name,ipp-attribute-fidelity,document-name,compression,
     *            document -format,document-natural-language,job-impressions
     *            ,job-media-sheets, job-template-attributes
     * @return IPP header
     * @throws UnsupportedEncodingException
     */

    public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
        if (url == null) {
            log.error("IppPrintJobOperation.getIppHeader(): uri is null");
            return null;
        }

        ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
        ippBuf = IppTag.getOperation(ippBuf, operationID);
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url));

        if (map == null) {
            ippBuf = IppTag.getEnd(ippBuf);
            ippBuf.flip();
            return ippBuf;
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));

        if (map.containsKey("job-name")) {
            ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "job-name", map.get("job-name"));
        }
        if (map.containsKey("ipp-attribute-fidelity")) {
            boolean value = map.get("ipp-attribute-fidelity").equals("true");
            ippBuf = IppTag.getBoolean(ippBuf, "ipp-attribute-fidelity", value);
        }
        if (map.containsKey("document-name")) {
            ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-name", map.get("document-name"));
        }
        if (map.containsKey("compression")) {
            ippBuf = IppTag.getKeyword(ippBuf, "compression", map.get("compression"));
        }
        if (map.containsKey("document-format")) {
            ippBuf = IppTag.getMimeMediaType(ippBuf, "document-format", map.get("document-format"));
        }
        if (map.containsKey("document-natural-language")) {
            ippBuf = IppTag.getNaturalLanguage(ippBuf, "document-natural-language", map.get("document-natural-language"));
        }
        if (map.containsKey("job-k-octets")) {
            int value = Integer.parseInt(map.get("job-k-octets"));
            ippBuf = IppTag.getInteger(ippBuf, "job-k-octets", value);
        }
        if (map.containsKey("job-impressions")) {
            int value = Integer.parseInt(map.get("job-impressions"));
            ippBuf = IppTag.getInteger(ippBuf, "job-impressions", value);
        }
        if (map.containsKey("job-media-sheets")) {
            int value = Integer.parseInt(map.get("job-media-sheets"));
            ippBuf = IppTag.getInteger(ippBuf, "job-media-sheets", value);
        }
        if (map.containsKey("job-attributes")) {
            String[] attributeBlocks = map.get("job-attributes").split("#");
            ippBuf = getJobAttributes(ippBuf, attributeBlocks);
        }

        ippBuf = IppTag.getEnd(ippBuf);
        ippBuf.flip();
        return ippBuf;
    }

}
