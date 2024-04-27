/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 23.03.2018 by oboehm (ob@oasd.de)
 */
package org.cups4j.operations.ipp;

import ch.ethz.vppserver.ippclient.IppBuffer;
import ch.ethz.vppserver.ippclient.IppResponse;
import ch.ethz.vppserver.ippclient.IppResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.operations.IppHttp;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.cups4j.operations.IppHttp.CUPS_TIMEOUT;

/**
 * The class IppSendDocumentOperation represents the operation for sending
 * a document.
 *
 * @author oboehm
 * @since 0.7.2 (23.03.2018)
 */
@Slf4j
public class IppSendDocumentOperation extends IppPrintJobOperation {

    public IppSendDocumentOperation() {
        operationID = SEND_DOCUMENT;
    }

    public IppSendDocumentOperation(int port) {
        this();
        ippPort = port;
    }

    private static IppResult getIppResult(CloseableHttpResponse httpResponse) throws IOException {
        try (InputStream istream = httpResponse.getEntity().getContent()) {
            byte[] result = IOUtils.toByteArray(istream);
            IppResponse ippResponse = new IppResponse();
            IppResult ippResult = ippResponse.getResponse(ByteBuffer.wrap(result));
            ippResult.setHttpStatusCode(httpResponse.getCode());
            if (ippResult.getHttpStatusCode() == 426) {
                ippResult.setHttpStatusResponse(new String(result));
                log.warn("Received {} after send-document.", ippResult);
            } else {
                ippResult.setHttpStatusResponse(new StatusLine(httpResponse).toString());
            }
            return ippResult;
        }
    }

    public IppResult request(CupsPrinter printer, URL printerURL, PrintJob printJob, CupsAuthentication creds,
                             int jobId, boolean lastDocument) {
        InputStream document = printJob.getDocument();
        String userName = printJob.getUserName();
        String jobName = printJob.getJobName();
        int copies = printJob.getCopies();
        String pageRanges = printJob.getPageRanges();
        String resolution = printJob.getResolution();

        String pageFormat = printJob.getPageFormat();
        boolean color = printJob.isColor();
        boolean portrait = printJob.isPortrait();

        Map<String, String> attributes = printJob.getAttributes();

        attributes.put("job-id", String.valueOf(jobId));
        attributes.put("last-document", String.valueOf(lastDocument));

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER;
        }

        attributes.put("requesting-user-name", userName);
        attributes.put("job-name", jobName);

        String copiesString;
        StringBuilder rangesString = new StringBuilder();
        if (copies > 0) { // other values are considered bad value by CUPS
            copiesString = "copies:integer:" + copies;
            addAttribute(attributes, "job-attributes", copiesString);
        }
        addAttribute(attributes, "job-attributes", portrait ? "orientation-requested:enum:3" : "orientation-requested:enum:4");
        addAttribute(attributes, "job-attributes", color ? "output-mode:keyword:color" : "output-mode:keyword:monochrome");

        if (isNotEmpty(pageFormat)) {
            addAttribute(attributes, "job-attributes", "media:keyword:" + pageFormat);
        }

        if (isNotEmpty(resolution)) {
            addAttribute(attributes, "job-attributes", "printer-resolution:resolution:" + resolution);
        }

        if (isNotBlank(pageRanges) && !"1-".equals(pageRanges.trim())) {
            String[] ranges = pageRanges.split(",");

            String delimeter = "";

            rangesString.append("page-ranges:setOfRangeOfInteger:");
            for (String range : ranges) {
                range = range.trim();

                String[] values = range.split("-");
                if (values.length == 1) {
                    range = range + "-" + range;
                }

                rangesString.append(delimeter).append(range);
                // following ranges need to be separated with ","
                delimeter = ",";
            }
            addAttribute(attributes, "job-attributes", rangesString.toString());
        }

        if (printJob.isDuplex()) {
            addAttribute(attributes, "job-attributes", "sides:keyword:two-sided-long-edge");
        }
        try {
            IppResult ippResult = request(printer, printerURL, attributes, document, creds);
            if (ippResult.getHttpStatusCode() >= 300) {
                String msg = "";
                List<AttributeGroup> attributeGroupList = ippResult.getAttributeGroupList();
                if (!attributeGroupList.isEmpty()) {
                    msg = " (" + attributeGroupList.get(0).getAttributes("status-message").getValue() + ")";
                }
                throw new IllegalStateException(
                        "IPP request to " + printerURL + " was not successful: " + ippResult.getHttpStatusResponse() +
                                msg);
            }
            return ippResult;
        } catch (IOException ex) {
            throw new IllegalStateException("request to " + printerURL + " failed", ex);
        }
    }

    private void addAttribute(Map<String, String> map, String name, String value) {
        if (value != null && name != null) {
            if (map.containsKey(name)) {
                map.put(name, map.get(name) + "#" + value);
            } else {
                map.put(name, value);
            }
        }
    }

    @Override
    public IppResult request(CupsPrinter printer, URL url, Map<String, String> map,
                             InputStream document, CupsAuthentication creds) throws IOException {
        ByteBuffer ippHeader = getIppHeader(url, map);
        try {
            IppResult ippResult = sendRequest(printer, url.toURI(), ippHeader, document, creds);
            if ((ippResult.getHttpStatusCode() == 426) && "http".equalsIgnoreCase(url.getProtocol())) {
                URI https = URI.create(url.toURI().toString().replace("http", "https"));
                log.warn("Access with {} failed - will try now {} as printerURL.", url, https);
                ippResult = sendRequest(printer, https, getIppHeader(url, map), document, creds);
            }
            return ippResult;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("cannot handle " + url + " as URI", ex);
        }
    }

    /**
     * Creates the IPP header with the IPP tags.
     *
     * @param url printer-uri
     * @param map attributes map
     * @return IPP header
     * @throws UnsupportedEncodingException in case of unsupported encoding
     */
    @Override
    public ByteBuffer getIppHeader(@NonNull URL url, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);
        ippBuf.putUri("printer-uri", url.toString());

        int jobId = Integer.parseInt(map.get("job-id"));
        ippBuf.putInteger("job-id", jobId);

        boolean lastDocument = map.get("last-document").equalsIgnoreCase("true");
        ippBuf.putBoolean("last-document", lastDocument);

        String userName = map.get("requesting-user-name");
        if (userName == null) {
            userName = System.getProperty("user.name", CupsClient.DEFAULT_USER);
        }
        ippBuf.putNameWithoutLanguage("requesting-user-name", userName);
        ippBuf.putNameWithoutLanguage("job-name", map.get("job-name"));

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

        String[] attributeBlocks = map.get("job-attributes").split("#");
        ippBuf.putJobAttributes(attributeBlocks);

        return ippBuf.getData();
    }

    private IppResult sendRequest(CupsPrinter printer, URI uri, ByteBuffer ippBuf,
                                  InputStream documentStream, CupsAuthentication creds) throws IOException {
        HttpPost httpPost = new HttpPost(uri);

        httpPost.setConfig(RequestConfig.custom().setResponseTimeout(CUPS_TIMEOUT).build());

        byte[] bytes = new byte[ippBuf.limit()];
        ippBuf.get(bytes);
        ByteArrayInputStream headerStream = new ByteArrayInputStream(bytes);

        InputStream inputStream = new SequenceInputStream(headerStream, documentStream);
        InputStreamEntity requestEntity = new InputStreamEntity(inputStream, -1, IPP_MIME_TYPE);
        httpPost.setEntity(requestEntity);
        IppHttp.setHttpHeaders(httpPost, printer, creds);

        ConnectionConfig config = ConnectionConfig.custom()
                .setConnectTimeout(CUPS_TIMEOUT)
                .setSocketTimeout(CUPS_TIMEOUT)
                .build();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(config)
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();
             CloseableHttpResponse httpResponse = client.execute(httpPost)) {
            log.debug("Received from {}: {}", uri, httpResponse);
            return getIppResult(httpResponse);
        }
    }

}
