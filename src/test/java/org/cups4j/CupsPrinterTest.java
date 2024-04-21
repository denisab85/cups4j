package org.cups4j;

import cups4j.TestCups;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link CupsPrinter} class.
 *
 * @author oboehm
 */
@Slf4j
public final class CupsPrinterTest {

    private CupsPrinter printer;

    /**
     * Gets a printer for testing. This is either the printer defined by the
     * system property 'printer' or the default printer.
     *
     * @return the printer
     * @throws Exception
     */
    public static CupsPrinter getPrinter() throws Exception {
        String name = System.getProperty("printer", new CupsClient().getDefaultPrinter().getName());
        if (name == null) {
            log.info("To specify printer please set system property 'printer'.");
        }
        return getPrinter(name);
    }

    /**
     * Returns the printer with the given name. The search of the name is
     * not case sensitiv.
     *
     * @param name name of the printer
     * @return printer
     */
    public static CupsPrinter getPrinter(String name) {
        try {
            List<CupsPrinter> printers = TestCups.getCupsClient().getPrinters();
            for (CupsPrinter p : printers) {
                if (name.equalsIgnoreCase(p.getName())) {
                    return p;
                }
            }
            throw new IllegalArgumentException("not a valid printer name: " + name);
        } catch (Exception ex) {
            throw new IllegalStateException("cannot get printers", ex);
        }
    }

    private static String generateJobnameFor(File file) {
        String basename = file.getName().split("\\.")[0];
        return generateJobNameFor(basename);
    }

    private static String generateJobNameFor(String basename) {
        byte[] epochTime = Base64.encodeBase64(BigInteger.valueOf(System.currentTimeMillis()).toByteArray());
        return basename + new String(epochTime).substring(2);
    }

    @BeforeEach
    public void setUpPrinter() throws Exception {
        printer = getPrinter();
        assertNotNull(printer);
        log.info("Printer {} was choosen for testing.", printer);
    }

    @Test
    @Disabled
    public void testPrintPDF() {
        print(printer, new File("src/test/resources/test.pdf"));
    }

    @Test
    @Disabled
    public void testPrintText() {
        print(printer, new File("src/test/resources/test.txt"));
    }

    private PrintRequestResult print(CupsPrinter printer, File file) {
        PrintJob job = createPrintJob(file);
        log.info("Print job '{}' will be sent to {}.", job, printer);
        try {
            return printer.print(job);
        } catch (Exception ex) {
            throw new IllegalStateException("print of '" + file + "' failed", ex);
        }
    }

    @Test
    @Disabled
    public void testPrintList() {
        File file = new File("src/test/resources/test.txt");
        printer.print(createPrintJob(file), createPrintJob(file));
    }

    @Test
    @Disabled
    public void testPrintListWithDifferentUsers() {
        File file = new File("src/test/resources/test.txt");
        assertThrows(IllegalStateException.class,
                () -> printer.print(createPrintJob(file, "oli"), createPrintJob(file, "stan")));
    }

    private PrintJob createPrintJob(File file) {
        return createPrintJob(file, CupsClient.DEFAULT_USER);
    }

    @Test
    @Disabled
    public void testPrintListWithNoUser() {
        PrintJob job = PrintJob.builder().document(new ByteArrayInputStream("secret".getBytes()))
                .jobName("testPrintListWithNoUser").build();
        printer.print(job, job);
    }

    private PrintJob createPrintJob(File file, String userName) {
        String jobName = generateJobnameFor(file);
        try (InputStream content = Files.newInputStream(file.toPath())) {
            return PrintJob.builder().document(content).jobName(jobName).userName(userName).build();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("cannot read '" + file + "'", ioe);
        }
    }

}
