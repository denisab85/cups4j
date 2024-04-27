package org.cups4j.operations.ipp;

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

import ch.ethz.vppserver.ippclient.IppBuffer;
import ch.ethz.vppserver.ippclient.IppResult;
import lombok.NonNull;
import org.cups4j.*;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IppGetJobsOperation extends IppOperation {

    public IppGetJobsOperation() {
        operationID = GET_JOBS;
    }

    public IppGetJobsOperation(int port) {
        this();
        ippPort = port;
    }

    /**
     * @param url printer-uri
     * @param map attributes i.e. requesting-user-name,limit,which-jobs,my-jobs,
     *            requested-attributes
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(@NonNull URL url, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);

        map.put("requested-attributes", "job-name job-id job-state job-originating-user-name job-printer-uri copies");

        ippBuf.putUri("printer-uri", stripPortNumber(url));

        ippBuf.putNameWithoutLanguage("requesting-user-name", map.get("requesting-user-name"));

        if (map.containsKey("limit")) {
            int value = Integer.parseInt(map.get("limit"));
            ippBuf.putInteger("limit", value);
        }

        if (map.containsKey("requested-attributes")) {
            String[] sta = map.get("requested-attributes").split(" ");
            ippBuf.putKeyword("requested-attributes", sta[0]);
            for (int i = 1; i < sta.length; i++) {
                ippBuf.putKeyword(null, sta[i]);
            }
        }

        if (map.containsKey("which-jobs")) {
            ippBuf.putKeyword("which-jobs", map.get("which-jobs"));
        }

        if (map.containsKey("my-jobs")) {
            boolean value = map.get("my-jobs").equals("true");
            ippBuf.putBoolean("my-jobs", value);
        }

        return ippBuf.getData();
    }

    public List<PrintJobAttributes> getPrintJobs(CupsPrinter printer, WhichJobsEnum whichJobs, String userName,
                                                 boolean myJobs, CupsAuthentication creds) throws Exception {
        List<PrintJobAttributes> jobs = new ArrayList<>();
        PrintJobAttributes jobAttributes;
        Map<String, String> map = new HashMap<>();

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER;
        }
        map.put("requesting-user-name", userName);
        map.put("which-jobs", whichJobs.getValue());
        if (myJobs) {
            map.put("my-jobs", "true");
        }
        map.put("requested-attributes",
                "page-ranges print-quality sides job-uri job-id job-state job-printer-uri job-name job-originating-user-name");

        IppResult result = request(printer, printer.getPrinterURL(), map, creds);

        for (AttributeGroup group : result.getAttributeGroupList()) {
            if ("job-attributes-tag".equals(group.getTagName())) {
                jobAttributes = new PrintJobAttributes();
                for (Attribute attr : group.getAttributes()) {
                    if (attr.getAttributeValues() != null && !attr.getAttributeValues().isEmpty()) {
                        String attValue = getAttributeValue(attr);
                        switch (attr.getName()) {
                            case "job-uri":
                                jobAttributes.setJobURL(new URL(attValue.replace("ipp://", "http://")));
                                break;
                            case "job-id":
                                jobAttributes.setJobID(Integer.parseInt(attValue));
                                break;
                            case "job-state":
                                jobAttributes.setJobState(JobStateEnum.fromString(attValue));
                                break;
                            case "job-printer-uri":
                                jobAttributes.setPrinterURL(new URL(attValue.replace("ipp://", "http://")));
                                break;
                            case "job-name":
                                jobAttributes.setJobName(attValue);
                                break;
                            case "job-originating-user-name":
                                jobAttributes.setUserName(attValue);
                                break;
                        }
                    }
                }
                jobs.add(jobAttributes);
            }
        }
        return jobs;
    }

}
