package org.cups4j;

import cups4j.TestCups;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link CupsClient} class.
 *
 * @author oliver (boehm@javatux.de)
 */
public class CupsClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);
    private static CupsClient client;

    @BeforeClass
    public static void setUpClient() throws Exception {
        client = TestCups.getCupsClient();
    }

    @Test
    public void getPrinters() throws Exception {
        List<CupsPrinter> printers = client.getPrinters();

        for (CupsPrinter printer : printers) {
            LOG.info("printer: " + printer.getName() + "[isClass=" + printer.isPrinterClass() + "]");
        }

        assertFalse(printers.isEmpty());
    }


    @Test
    public void testMakeAndModel() throws Exception {
        List<CupsPrinter> printers = client.getPrinters();

        for (CupsPrinter printer : printers) {
            LOG.info("printer: " + printer.getName() + "[makeAndModel=" + printer.getMakeAndModel() + "]");
        }
    }


}
