package ch.ethz.vppserver.ippclient;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IppAttributeProviderFactory {
    public static IIppAttributeProvider createIppAttributeProvider() {
        return IppAttributeProvider.getInstance();
    }
}
