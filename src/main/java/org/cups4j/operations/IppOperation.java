package org.cups4j.operations;

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
import ch.ethz.vppserver.ippclient.IppResponse;
import ch.ethz.vppserver.ippclient.IppResult;
import lombok.NonNull;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.ipp.attributes.Attribute;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

public abstract class IppOperation {
    protected static final short PRINT_JOB = 0x0002;                   // Print a file.
    protected static final short VALIDATE_JOB = 0x0004;                // Validate job attributes.
    protected static final short CREATE_JOB = 0x0005;                  // Create a print job.
    protected static final short SEND_DOCUMENT = 0x0006;               // Send a file for a print job.
    protected static final short CANCEL_JOB = 0x0008;                  // Cancel a print job.
    protected static final short GET_JOB_ATTRIBUTES = 0x0009;          // Get job attributes.
    protected static final short GET_JOBS = 0x000A;                    // Get all jobs.
    protected static final short GET_PRINTER_ATTRIBUTES = 0x000B;      // Get printer attributes.
    protected static final short HOLD_JOB = 0x000C;                    // Hold a job for printing.
    protected static final short RELEASE_JOB = 0x000D;                 // Release a job for printing.
    protected static final short RESTART_JOB = 0x000E;                 // Restarts a print job.
    protected static final short PAUSE_PRINTER = 0x0010;               // Pause printing on a printer.
    protected static final short RESUME_PRINTER = 0x0011;              // Resume printing on a printer.
    protected static final short PURGE_JOBS = 0x0012;                  // Purge all jobs.
    protected static final short SET_PRINTER_ATTRIBUTES = 0x0013;      // Set attributes for a printer.
    protected static final short SET_JOB_ATTRIBUTES = 0x0014;          // Set attributes for a pending or held job.
    protected static final short CREATE_PRINTER_SUBSCRIPTION = 0x0016; // Creates a subscription associated with a printer or the server.
    protected static final short CREATE_JOB_SUBSCRIPTION = 0x0017;     // Creates a subscription associated with a job.
    protected static final short GET_SUBSCRIPTION_ATTRIBUTES = 0x0018; // Gets the attributes for a subscription.
    protected static final short GET_SUBSCRIPTIONS = 0x0019;           // Gets the attributes for zero or more subscriptions.
    protected static final short RENEW_SUBSCRIPTION = 0x001A;          // Renews a subscription.
    protected static final short CANCEL_SUBSCRIPTION = 0x001B;         // Cancels a subscription.
    protected static final short GET_NOTIFICATIONS = 0x001C;           // Get notification events for ippget subscriptions.
    protected static final short ENABLE_PRINTER = 0x0022;              // Accepts jobs on a printer.
    protected static final short DISABLE_PRINTER = 0x0023;             // Rejects jobs on a printer.
    protected static final short HOLD_NEW_JOBS = 0x0025;               // Hold new jobs by default.
    protected static final short RELEASE_HELD_NEW_JOBS = 0x0026;       // Releases all jobs that were previously held.
    protected static final short CANCEL_JOBS = 0x0038;                 // Cancel all jobs (administrator).
    protected static final short CANCEL_MY_JOBS = 0x0039;              // Cancel all jobs (user).
    protected static final short CLOSE_JOB = 0x003b;                   // Close a created job.
    protected static final short CUPS_GET_DEFAULT = 0x4001;            // Get the default destination.
    protected static final short CUPS_GET_PRINTERS = 0x4002;           // Get all the available printers.
    protected static final short CUPS_ADD_MODIFY_PRINTER = 0x4003;     // Add or modify a printer.
    protected static final short CUPS_DELETE_PRINTER = 0x4004;         // Delete a printer.
    protected static final short CUPS_GET_CLASSES = 0x4005;            // Get all the available printer classes.
    protected static final short CUPS_ADD_MODIFY_CLASS = 0x4006;       // Add or modify a printer class.
    protected static final short CUPS_DELETE_CLASS = 0x4007;           // Delete a printer class.
    protected static final short CUPS_SET_DEFAULT = 0x400A;            // Set the default destination.
    protected static final short CUPS_GET_DEVICES = 0x400B;            // Get all the available devices.
    protected static final short CUPS_GET_PPDS = 0x400C;               // Get all the available PPDs.
    protected static final short CUPS_MOVE_JOB = 0x400D;               // Move a job to a different printer.
    protected static final short CUPS_AUTHENTICATE_JOB = 0x400E;       // Authenticate a job for printing.
    protected static final short CUPS_GET_PPD = 0x400F;                // Get a PPD file.
    protected static final short CUPS_GET_DOCUMENT = 0x4027;           // Get a document file from a job.
    protected static final short CUPS_CREATE_LOCAL_PRINTER = 0x4028;   // Creates a local (temporary) print queue pointing to a remote IPP Everywhere printer.

    protected final static ContentType IPP_MIME_TYPE = ContentType.create("application/ipp");
    protected short operationID = -1; // IPP operation ID
    protected short bufferSize = 8192; // BufferSize for this operation
    protected int ippPort = CupsClient.DEFAULT_PORT;

    /**
     * Removes the port number in the submitted URL
     *
     * @param url
     * @return url without port number
     */
    protected static String stripPortNumber(URL url) {
        String protocol = url.getProtocol();
        if ("ipp".equals(protocol)) {
            protocol = "http";
        }

        return protocol + "://" + url.getHost() + url.getPath();
    }

    private static InputStreamEntity getInputStreamEntity(InputStream documentStream, byte[] bytes) {
        ByteArrayInputStream headerStream = new ByteArrayInputStream(bytes);

        // If we need to send a document, concatenate InputStreams
        InputStream inputStream = headerStream;
        if (documentStream != null) {
            inputStream = new SequenceInputStream(headerStream, documentStream);
        }

        // set length to -1 to advice the entity to read until EOF
        return new InputStreamEntity(inputStream, -1, IPP_MIME_TYPE);
    }

    /**
     * Gets the IPP header
     *
     * @param url
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(URL url) throws UnsupportedEncodingException {
        return getIppHeader(url, null);
    }

    public IppResult request(CupsPrinter printer, URL url, Map<String, String> map, CupsAuthentication creds)
            throws Exception {
        return sendRequest(printer, url, getIppHeader(url, map), creds);
    }

    public IppResult request(CupsPrinter printer, URL url, Map<String, String> map, InputStream document,
                             CupsAuthentication creds) throws Exception {
        return sendRequest(printer, url, getIppHeader(url, map), document, creds);
    }

    /**
     * Gets the IPP header
     *
     * @param url
     * @param map
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(@NonNull URL url, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);
        ippBuf.putUri("printer-uri", stripPortNumber(url));

        if (map != null) {
            ippBuf.putNameWithoutLanguage("requesting-user-name", map.get("requesting-user-name"));

            if (map.containsKey("limit")) {
                int value = Integer.parseInt(map.get("limit"));
                ippBuf.putInteger("limit", value);
            }

            if (map.containsKey("requested-attributes")) {
                String[] sta = map.get("requested-attributes").split(" ");
                ippBuf.putKeyword("requested-attributes", sta[0]);
                int l = sta.length;
                for (int i = 1; i < l; i++) {
                    ippBuf.putKeyword(null, sta[i]);
                }
            }
        }

        return ippBuf.getData();
    }

    /**
     * Sends a request to the provided URL
     *
     * @param url
     * @param ippBuf
     * @return result
     * @throws Exception
     */
    private IppResult sendRequest(CupsPrinter printer, URL url, ByteBuffer ippBuf, CupsAuthentication creds) throws Exception {
        IppResult result = sendRequest(printer, url, ippBuf, null, creds);
        if (result.getHttpStatusCode() >= 300) {
            throw new IOException("HTTP error! Status code:  " + result.getHttpStatusResponse());
        }
        return result;
    }

    /**
     * Sends a request to the provided url
     *
     * @param url
     * @param ippBuf
     * @param documentStream
     * @return result
     * @throws Exception
     */
    private IppResult sendRequest(CupsPrinter printer, URL url, ByteBuffer ippBuf, InputStream documentStream,
                                  CupsAuthentication creds) throws Exception {
        if (ippBuf == null || url == null) {
            return null;
        }

        final IppHttpResult ippHttpResult;
        byte[] result;
        CloseableHttpClient client = IppHttp.createHttpClient();

        HttpPost httpPost = new HttpPost(new URI("http://" + url.getHost() + ':' + ippPort) + url.getPath());
        IppHttp.setHttpHeaders(httpPost, printer, creds);

        byte[] bytes = new byte[ippBuf.limit()];
        ippBuf.get(bytes);

        InputStreamEntity requestEntity = getInputStreamEntity(documentStream, bytes);
        httpPost.setEntity(requestEntity);

        ippHttpResult = new IppHttpResult();
        ippHttpResult.setStatusCode(-1);

        HttpClientResponseHandler<byte[]> handler = response -> {
            HttpEntity entity = response.getEntity();
            ippHttpResult.setStatusLine(new StatusLine(response).toString());
            ippHttpResult.setStatusCode(response.getCode());
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            } else {
                return null;
            }
        };

        result = client.execute(httpPost, handler);

        IppResponse ippResponse = new IppResponse();

        IppResult ippResult = ippResponse.getResponse(ByteBuffer.wrap(result));
        ippResult.setHttpStatusResponse(ippHttpResult.getStatusLine());
        ippResult.setHttpStatusCode(ippHttpResult.getStatusCode());

        return ippResult;
    }

    protected String getAttributeValue(Attribute attr) {
        return attr.getAttributeValues().get(0).getValue();
    }

}
