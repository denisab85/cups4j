package org.cups4j.operations.cups;

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

import ch.ethz.vppserver.ippclient.IppResult;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsPrinter;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.operations.IppOperation;

import java.net.URL;
import java.util.HashMap;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class CupsGetDefaultOperation extends IppOperation {
    public CupsGetDefaultOperation() {
        operationID = 0x4001;
        bufferSize = 8192;
    }

    public CupsGetDefaultOperation(int port) {
        this();
        this.ippPort = port;
    }

    public CupsPrinter getDefaultPrinter(String hostname, int port, CupsAuthentication creds) throws Exception {
        CupsPrinter defaultPrinter = null;
        CupsGetDefaultOperation command = new CupsGetDefaultOperation(port);

        HashMap<String, String> map = new HashMap<>();
        map.put("requested-attributes", "printer-name printer-uri-supported printer-location");

        IppResult result = command.request(null, new URL("http://" + hostname + "/printers"), map, creds);
        for (AttributeGroup group : result.getAttributeGroupList()) {
            if (group.getTagName().equals("printer-attributes-tag")) {
                String printerURL = null;
                String printerName = null;
                String location = null;
                for (Attribute attr : group.getAttributes()) {
                    switch (attr.getName()) {
                        case "printer-uri-supported":
                            printerURL = attr.getAttributeValues().get(0).getValue().replace("ipp://", "http://");
                            break;
                        case "printer-name":
                            printerName = attr.getAttributeValues().get(0).getValue();
                            break;
                        case "printer-location":
                            if (isNotEmpty(attr.getAttributeValues())) {
                                location = attr.getAttributeValues().get(0).getValue();
                            }
                            break;
                    }
                }
                defaultPrinter = new CupsPrinter(creds, new URL(printerURL), printerName);
                defaultPrinter.setDefault(true);
                defaultPrinter.setLocation(location);
            }
        }

        return defaultPrinter;
    }
}
