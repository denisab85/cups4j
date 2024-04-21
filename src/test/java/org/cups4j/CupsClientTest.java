package org.cups4j;

import cups4j.TestCups;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.cups4j.operations.IppHttp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for {@link CupsClient} class.
 *
 * @author oliver (boehm@javatux.de)
 */
@ExtendWith(MockitoExtension.class)
public class CupsClientTest {

    private static CupsClient client;

    @Mock
    private MockedStatic<IppHttp> ippHttpMockedStatic;

    @Mock
    private CloseableHttpClient closeableHttpClientMocked;

    @BeforeAll
    public static void setUpClient() {
        client = TestCups.getCupsClient();
    }

    @BeforeEach
    public void setUpMocks() throws IOException {
        ippHttpMockedStatic.when(IppHttp::createHttpClient).thenReturn(closeableHttpClientMocked);
        InputStream stream = getClass().getResourceAsStream("/ipp/IppResponsePrinters.bin");
        doReturn(IOUtils.toByteArray(stream))
                .when(closeableHttpClientMocked).execute(any(HttpPost.class), any(HttpClientResponseHandler.class));
    }

    @Test
    public void getPrinters() throws Exception {
        List<CupsPrinter> printers = client.getPrinters();
        assertFalse(printers.isEmpty());
    }


    @Test
    public void testMakeAndModel() throws Exception {
        List<CupsPrinter> printers = client.getPrinters();
        for (CupsPrinter printer : printers) {
            assertNotNull(printer.getName(), "Printer name");
            assertNotNull(printer.getMakeAndModel(), "Make and model");
        }
    }

}