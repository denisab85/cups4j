package ch.ethz.vppserver.ippclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IppAttributeProviderTest {

    @Test
    void getInstance() {
        IppAttributeProvider instance = IppAttributeProvider.getInstance();
        assertNotNull(instance);
    }

}
