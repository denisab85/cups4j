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

import ch.ethz.vppserver.ippclient.IppResponse;
import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public abstract class IppOperation {
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
    public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
        if (url == null) {
            log.error("IppGetJObsOperation.getIppHeader(): uri is null");
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

        if (map.containsKey("limit")) {
            int value = Integer.parseInt(map.get("limit"));
            ippBuf = IppTag.getInteger(ippBuf, "limit", value);
        }

        if (map.containsKey("requested-attributes")) {
            String[] sta = map.get("requested-attributes").split(" ");
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0]);
            int l = sta.length;
            for (int i = 1; i < l; i++) {
                ippBuf = IppTag.getKeyword(ippBuf, null, sta[i]);
            }
        }

        ippBuf = IppTag.getEnd(ippBuf);
        ippBuf.flip();
        return ippBuf;
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
