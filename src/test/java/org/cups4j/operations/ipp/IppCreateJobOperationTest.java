package org.cups4j.operations.ipp;

import ch.ethz.vppserver.ippclient.IppResult;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.CupsPrinter;
import org.cups4j.CupsPrinterTest;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-tests for {@link IppCreateJobOperation} class.
 *
 * @author oboehm
 * @since 0.7.2 (23.03.2018)
 */
@Slf4j
public class IppCreateJobOperationTest extends AbstractIppOperationTest {

    private final IppCreateJobOperation operation = new IppCreateJobOperation();

    private static byte[] toByteArray(ByteBuffer buffer) {
        byte[] array = new byte[buffer.limit()];
        buffer.get(array);
        return array;
    }

    private static void checkAttribute(IppResult ippResult, String name) {
        for (AttributeGroup attributeGroup : ippResult.getAttributeGroupList()) {
            if (hasAttribute(attributeGroup.getAttribute(), name)) {
                log.info("Attribute '{}' was found in {}.", name, ippResult.getAttributeGroupList());
                return;
            }
        }
        fail("Attribute '" + name + "' not found in " + ippResult.getAttributeGroupList());
    }

    private static boolean hasAttribute(List<Attribute> attributes, String name) {
        for (Attribute attr : attributes) {
            if (name.equals(attr.getName())) {
                log.info("{} = {}", name, attr.getAttributeValue());
                return true;
            }
        }
        return false;
    }

    @Test
    public void testOperationId() throws UnsupportedEncodingException {
        ByteBuffer buffer = getIppHeader(operation);
        assertEquals(5, buffer.get(3));
    }

    @Test
    public void testGetIppHeader() throws UnsupportedEncodingException {
        URL printerURL = createURL("http://localhost:631/test-printer");
        ByteBuffer buffer = operation.getIppHeader(printerURL);
        checkAttribute(buffer, "printer-uri", "http://localhost:631/test-printer");
        checkAttribute(buffer, "requesting-user-name", System.getProperty("user.name", "anonymous"));

    }

    @Test
    public void testGetIppHeaderWithJobName() throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        map.put("job-name", "Test-Job");
        ByteBuffer buffer = operation.getIppHeader(createURL("http://localhost:631/test-printer"), map);
        checkAttribute(buffer, "job-name", "Test-Job");
    }

    @Disabled
    public void testRequest() throws Exception {
        CupsPrinter cupsPrinter = CupsPrinterTest.getPrinter();
        if (cupsPrinter == null) {
            log.warn("No default printer found for testing - run test with '-Dprinter=...' to define it.");
            return;
        }
        IppResult ippResult = operation.request(cupsPrinter, cupsPrinter.getPrinterURL(), null);
        assertNotNull(ippResult);
        checkAttribute(ippResult, "job-uri");
        checkAttribute(ippResult, "job-id");
        checkAttribute(ippResult, "job-state");
        checkAttribute(ippResult, "job-state-reasons");
    }

}
