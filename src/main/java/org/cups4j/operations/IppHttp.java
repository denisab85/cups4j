package org.cups4j.operations;

import lombok.experimental.UtilityClass;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.utils.Base64;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.util.Timeout;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsPrinter;

import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@UtilityClass
public final class IppHttp {

    private static final int MAX_CONNECTION_BUFFER = 20;

    public static final Timeout CUPS_TIMEOUT = Timeout.ofMilliseconds(Integer.parseInt(System.getProperty("cups4j.timeout", "10000")));

    private static final RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(CUPS_TIMEOUT).build();

    private static final CloseableHttpClient client;

    static {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(MAX_CONNECTION_BUFFER)
                .setMaxConnTotal(MAX_CONNECTION_BUFFER)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(CUPS_TIMEOUT)
                        .setSocketTimeout(CUPS_TIMEOUT)
                        .build())
                .build();
        client = HttpClients.custom()
                .disableRedirectHandling()
                .disableCookieManagement()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()
                .setRetryStrategy(DefaultHttpRequestRetryStrategy.INSTANCE)
                .build();
    }

    public static CloseableHttpClient createHttpClient() {
        return client;
    }

    public static void setHttpHeaders(HttpPost httpPost, CupsPrinter targetPrinter, CupsAuthentication creds) {
        httpPost.addHeader("target-group", targetPrinter == null ? "local" : targetPrinter.getName());
        httpPost.setConfig(requestConfig);

        if (creds != null && isNotBlank(creds.getUserid()) && isNotBlank(creds.getPassword())) {
            String auth = creds.getUserid() + ':' + creds.getPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }

}
