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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.cups4j.CupsAuthentication;
import org.cups4j.CupsPrinter;
import org.cups4j.PrinterStateEnum;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.ipp.attributes.AttributeValue;
import org.cups4j.operations.IppOperation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * CUPS-Get-Printers operation (0x4002) returns the printer attributes for every printer known to the system.
 * This may include printers that are not served directly by the server.
 * <p>
 * <h6>CUPS-Get-Printers Request</h6>
 * <p>
 * The following groups of attributes are supplied as part of the CUPS-Get-Printers request: <p>
 * <h7>Group 1: Operation Attributes</h7>
 *          <ul>
 *              <li>Natural Language and Character Set: <p>
 *                  The "attributes-charset" and "attributes-natural-language" attributes as described in
 *                  section 3.1.4.1 of the IPP Model and Semantics document.</li>
 *              <li>"first-printer-name" (name(127)): CUPS 1.2/macOS 10.5 <p>
 *                  The client OPTIONALLY supplies this
 *                  attribute to select the first printer that is returned.</li>
 *              <li>"limit" (integer (1:MAX)): <p>
 *                  The client OPTIONALLY supplies this attribute
 *                  limiting the number of printers that are returned.</li>
 *              <li>"printer-id" (integer(0:65535)): CUPS 2.2 <p>
 *                  The client OPTIONALLY supplies this
 *                  attribute to select which printer is returned.</li>
 *              <li>"printer-location" (text(127)): CUPS 1.1.7 <p>
 *                  The client OPTIONALLY supplies this
 *                  attribute to select which printers are returned.</li>
 *              <li>"printer-type" (type2 enum): CUPS 1.1.7 -  <p>
 *                  The client OPTIONALLY supplies a printer type
 *                  enumeration to select which printers are returned.</li>
 *              <li>"printer-type-mask" (type2 enum): CUPS 1.1.7 <p>
 *                  The client OPTIONALLY supplies a printer type
 *                  mask enumeration to select which bits are used in the "printer-type" attribute.</li>
 *              <li>"requested-attributes" (1setOf keyword):  <p>
 *                  The client OPTIONALLY supplies a set of attribute names
 *                  and/or attribute group names in whose values the requester is interested. If the client omits this
 *                  attribute, the server responds as if this attribute had been supplied with a value of 'all'.</li>
 *              <li>"requested-user-name" (name(127)): CUPS 1.2/macOS 10.5 <p>
 *                  The client OPTIONALLY supplies a
 *                  user name that is used to filter the returned printers.</li>
 *          </ul>
 *
 * <h6>CUPS-Get-Printers Response</h6>
 * The following groups of attributes are send as part of the CUPS-Get-Printers Response: <p>
 *      <h7>Group 1: Operation Attributes</h7>
 *          <ul>
 *              <li>Natural Language and Character Set: The "attributes-charset" and
 *                  "attributes-natural-language" attributes as described in section 3.1.4.2 of the
 *                  IPP Model and Semantics document.</li>
 *              <li>Status Message: The standard response status message.</li>
 *          </ul>
 *      <h7>Group 2: Printer Object Attributes</h7>
 *          <ul>
 *              <li>The set of requested attributes and their current values for each printer.</li>
 *          </ul>
 */
@Slf4j
public class CupsGetPrintersOperation extends IppOperation {

    public CupsGetPrintersOperation() {
        operationID = CUPS_GET_PRINTERS;
    }

    public CupsGetPrintersOperation(int port) {
        this();
        this.ippPort = port;
    }

    public List<CupsPrinter> getPrinters(String hostname, int port, CupsAuthentication creds) throws Exception {
        List<CupsPrinter> printers = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("requested-attributes",
                "copies-supported page-ranges-supported printer-name printer-info printer-state printer-location printer-make-and-model printer-uri-supported media-supported media-default sides-supported sides-default orientation-requested-supported printer-resolution-supported printer printer-resolution-default number-up-default number-up-supported document-format-supported print-color-mode-supported print-color-mode-default device-uri");
        this.ippPort = port;

        IppResult result = request(null, new URL("http://" + hostname + ':' + port + "/printers"), map, creds);

        for (AttributeGroup group : result.getAttributeGroupList()) {
            if (group.getTagName().equals("printer-attributes-tag")) {
                String printerURI = null;
                String printerName = null;
                String printerLocation = null;
                String printerDescription = null;
                PrinterStateEnum printerState = null;
                List<String> mediaSupportedList = new ArrayList<>();
                String mediaDefault = null;
                List<String> printerResolutionSupported = new ArrayList<>();
                String printerResolutionDefault = null;
                List<String> printerColorModeSupported = new ArrayList<>();
                String printerColorModeDefault = null;
                List<String> mimeTypesSupported = new ArrayList<>();
                String sidesDefault = null;
                List<String> sidesSupported = new ArrayList<>();
                String numberUpDefault = null;
                List<String> numberUpSupported = new ArrayList<>();
                String deviceURI = null;
                String printerMakeAndModel = null;

                for (Attribute attr : group.getAttributes()) {
                    switch (attr.getName()) {
                        case "printer-uri-supported":
                            printerURI = getAttributeValue(attr).replace("ipp://", "http://");
                            printerURI = StringUtils.remove(printerURI, "http://");
                            printerURI = StringUtils.substringAfter(printerURI, "/");
                            printerURI = "http://" + hostname + ':' + port + "/" + printerURI;
                            break;
                        case "printer-name":
                            printerName = getAttributeValue(attr);
                            break;
                        case "printer-location":
                            printerLocation = getAttributeValue(attr);
                            break;
                        case "printer-info":
                            printerDescription = getAttributeValue(attr);
                            break;
                        case "device-uri":
                            deviceURI = getAttributeValue(attr);
                            break;
                        case "printer-state":
                            printerState = PrinterStateEnum.fromStringInteger(getAttributeValue(attr));
                            break;
                        case "media-default":
                            mediaDefault = getAttributeValue(attr);
                            break;
                        case "media-supported":
                            mediaSupportedList = getAttributeValues(attr);
                            break;
                        case "number-up-default":
                            numberUpDefault = getAttributeValue(attr);
                            break;
                        case "number-up-supported":
                            numberUpSupported = getAttributeValues(attr);
                            break;
                        case "printer-resolution-default":
                            printerResolutionDefault = getAttributeValue(attr);
                            break;
                        case "printer-resolution-supported":
                            printerResolutionSupported = getAttributeValues(attr);
                            break;
                        case "print-color-mode-default":
                            printerColorModeDefault = getAttributeValue(attr);
                            break;
                        case "print-color-mode-supported":
                            printerColorModeSupported = getAttributeValues(attr);
                            break;
                        case "document-format-supported":
                            mimeTypesSupported = getAttributeValues(attr);
                            break;
                        case "sides-supported":
                            sidesSupported = getAttributeValues(attr);
                            break;
                        case "sides-default":
                            sidesDefault = getAttributeValue(attr);
                            break;
                        case "printer-make-and-model":
                            printerMakeAndModel = getAttributeValue(attr);
                            break;
                    }
                }
                URL printerUrl;
                try {
                    printerUrl = new URL(printerURI);
                } catch (Throwable t) {
                    log.error("Error encountered building URL from printer uri of printer {}," +
                                    " uri returned was [{}].  Attribute group tag/description: [{}/{}]",
                            printerName, printerURI, group.getTagName(), group.getDescription(), t);
                    throw new Exception(t);
                }

                CupsPrinter printer = new CupsPrinter(creds, printerUrl, printerName);
                printer.setState(printerState);
                printer.setLocation(printerLocation);
                printer.setDescription(printerDescription);
                printer.setDeviceUri(deviceURI);
                printer.setMediaDefault(mediaDefault);
                printer.setMediaSupported(mediaSupportedList);
                printer.setResolutionDefault(printerResolutionDefault);
                printer.setResolutionSupported(printerResolutionSupported);
                printer.setColorModeDefault(printerColorModeDefault);
                printer.setColorModeSupported(printerColorModeSupported);
                printer.setMimeTypesSupported(mimeTypesSupported);
                printer.setSidesDefault(sidesDefault);
                printer.setSidesSupported(sidesSupported);
                printer.setNumberUpDefault(numberUpDefault);
                printer.setNumberUpSupported(numberUpSupported);
                printer.setMakeAndModel(printerMakeAndModel);

                printers.add(printer);
            }
        }

        return printers;
    }

    protected List<String> getAttributeValues(Attribute attr) {
        if (attr.getAttributeValues() == null) {
            return new ArrayList<>();
        }
        return attr.getAttributeValues().stream().map(AttributeValue::getValue).collect(Collectors.toList());
    }

    protected String getAttributeValue(Attribute attr) {
        String result = null;
        if (isNotEmpty(attr.getAttributeValues())) {
            result = attr.getAttributeValues().get(0).getValue();
        }
        return result;
    }
}
