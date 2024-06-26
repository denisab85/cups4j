package org.cups4j.operations.ipp;

/**
 * Copyright (C) 2009 Harald Weyhing
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

import ch.ethz.vppserver.ippclient.IppTag;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

public class IppGetPrinterAttributesOperation extends IppOperation {

    public IppGetPrinterAttributesOperation() {
        operationID = 0x000b;
        bufferSize = 8192;
    }


    public IppGetPrinterAttributesOperation(int port) {
        this();
        ippPort = port;
    }

    /**
     * @param url printer-uri
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(String url) throws UnsupportedEncodingException {
        return getIppHeader(url, null);
    }

    /**
     * @param url printer-uri
     * @param map attributes i.e.
     *            requesting-user-name,requested-attributes,document-format
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(String url, Map<String, String> map) throws UnsupportedEncodingException {
        ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);

        ippBuf = IppTag.getOperation(ippBuf, operationID);
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", url);

        if (map == null) {
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", "all");
            ippBuf = IppTag.getEnd(ippBuf);
            ippBuf.flip();
            return ippBuf;
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));
        if (map.containsKey("requested-attributes")) {
            String[] sta = map.get("requested-attributes").split(" ");
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0]);
            int l = sta.length;
            for (int i = 1; i < l; i++) {
                ippBuf = IppTag.getKeyword(ippBuf, null, sta[i]);
            }
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-format", map.get("document-format"));

        ippBuf = IppTag.getEnd(ippBuf);
        ippBuf.flip();
        return ippBuf;
    }
}
