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

import ch.ethz.vppserver.ippclient.IppBuffer;
import lombok.NonNull;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

public class IppPrintJobOperation extends IppOperation {

    public IppPrintJobOperation() {
        operationID = PRINT_JOB;
    }

    public IppPrintJobOperation(int port) {
        this();
        this.ippPort = port;
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

    public ByteBuffer getIppHeader(@NonNull URL url, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);
        ippBuf.putUri("printer-uri", stripPortNumber(url));

        if (map != null) {
            ippBuf.putNameWithoutLanguage("requesting-user-name", map.get("requesting-user-name"));

            if (map.containsKey("job-name")) {
                ippBuf.putNameWithoutLanguage("job-name", map.get("job-name"));
            }
            if (map.containsKey("ipp-attribute-fidelity")) {
                boolean value = map.get("ipp-attribute-fidelity").equals("true");
                ippBuf.putBoolean("ipp-attribute-fidelity", value);
            }
            if (map.containsKey("document-name")) {
                ippBuf.putNameWithoutLanguage("document-name", map.get("document-name"));
            }
            if (map.containsKey("compression")) {
                ippBuf.putKeyword("compression", map.get("compression"));
            }
            if (map.containsKey("document-format")) {
                ippBuf.putMimeMediaType("document-format", map.get("document-format"));
            }
            if (map.containsKey("document-natural-language")) {
                ippBuf.putNaturalLanguage("document-natural-language", map.get("document-natural-language"));
            }
            if (map.containsKey("job-k-octets")) {
                int value = Integer.parseInt(map.get("job-k-octets"));
                ippBuf.putInteger("job-k-octets", value);
            }
            if (map.containsKey("job-impressions")) {
                int value = Integer.parseInt(map.get("job-impressions"));
                ippBuf.putInteger("job-impressions", value);
            }
            if (map.containsKey("job-media-sheets")) {
                int value = Integer.parseInt(map.get("job-media-sheets"));
                ippBuf.putInteger("job-media-sheets", value);
            }
            if (map.containsKey("job-attributes")) {
                String[] attributeBlocks = map.get("job-attributes").split("#");
                ippBuf.putJobAttributes(attributeBlocks);
            }
        }
        return ippBuf.getData();
    }

}
