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

/**
 * CUPS-Get-Default operation (0x4001) returns the default printer URI and attributes.
 * <p>
 * <h6>CUPS-Get-Default Request</h6>
 * <p>
 * The following groups of attributes are supplied as part of the CUPS-Get-Default request:
 * <p/>
 * <h7>Group 1: Operation Attributes</h7>
 * <ul>
 *     <li> Natural Language and Character Set: <p>
 *          The "attributes-charset" and "attributes-natural-language" attributes as described in section 3.1.4.1
 *          of the IPP Model and Semantics document.</li>
 *     <li> "requested-attributes" (1setOf keyword): <p>
 *          The client OPTIONALLY supplies a set of attribute names
 *          and/or attribute group names in whose values the requester is interested. If the client omits this
 *          attribute, the server responds as if this attribute had been supplied with a value of 'all'.
 *     </li>
 *  </ul>
 * <h6>CUPS-Get-Default Response</h6>
 * <p>
 * The following groups of attributes are sent as part of the CUPS-Get-Default Response:
 * <p/>
 * <h7>Group 1:  Operation Attributes</h7>
 * <ul>
 *      <li> Natural Language and Character Set: <p>
 *           The "attributes-charset" and "attributes-natural-language" attributes as described in section 3.1.4.2
 *           of the IPP Model and Semantics document.
 *      </li>
 *      <li> Status Message: <p>
 *          The standard response status message.
 *      </li>
 * </ul>
 * <h7>Group 2: Printer Object Attributes</h7>
 *              <ul>
 *                  <li> The set of requested attributes and their current values.
 *                </li>
 *           </ul>
 */
public class CupsGetDefaultOperation extends IppOperation {
    public CupsGetDefaultOperation() {
        operationID = CUPS_GET_DEFAULT;
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
