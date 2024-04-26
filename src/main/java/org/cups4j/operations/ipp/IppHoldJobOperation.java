package org.cups4j.operations.ipp;

/**
 * Copyright (C) 2011 Harald Weyhing
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
import ch.ethz.vppserver.ippclient.IppResult;
import lombok.NonNull;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintRequestResult;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class IppHoldJobOperation extends IppOperation {

    public IppHoldJobOperation() {
        operationID = HOLD_JOB;
    }

    public IppHoldJobOperation(int port) {
        this();
        this.ippPort = port;
    }

    /**
     * @param uri printer-uri
     * @param map attributes
     *            i.e.job-name,ipp-attribute-fidelity,document-name,compression,
     *            document -format,document-natural-language,job-impressions
     *            ,job-media-sheets, job-template-attributes
     * @return IPP header
     * @throws UnsupportedEncodingException
     */

    public ByteBuffer getIppHeader(@NonNull URL uri, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);

        if (map != null) {
            if (map.containsKey("job-id")) {
                ippBuf.putUri("printer-uri", stripPortNumber(uri));
                int jobId = Integer.parseInt(map.get("job-id"));
                ippBuf.putInteger("job-id", jobId);
            } else {
                ippBuf.putUri("job-uri", stripPortNumber(uri));
            }
            ippBuf.putNameWithoutLanguage("requesting-user-name", map.get("requesting-user-name"));
        }
        return ippBuf.getData();
    }

    /**
     * Cancels a print job on the IPP server running on the given host.
     *
     * @param hostname
     * @param userName
     * @param jobID
     * @param message
     * @return true on successful cancelation otherwise false.
     * @throws Exception
     */
    public boolean holdJob(String hostname, String userName, int jobID,
                           CupsPrinter printer, CupsAuthentication creds) throws Exception {

        Map<String, String> map = new HashMap<>();

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER;
        }
        map.put("requesting-user-name", userName);

        URL url = new URL("http://" + hostname + "/jobs/" + jobID);
        map.put("job-uri", url.toString());

        IppResult result = request(printer, url, map, creds);

        return new PrintRequestResult(result).isSuccessfulResult();
    }

}
