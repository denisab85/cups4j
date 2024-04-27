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
import org.cups4j.CupsAuthentication;
import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IppGetJobAttributesOperation extends IppOperation {

    public IppGetJobAttributesOperation() {
        operationID = GET_JOB_ATTRIBUTES;
    }

    public IppGetJobAttributesOperation(int port) {
        this();
        this.ippPort = port;
    }

    /**
     * @param uri printer-uri or job-uri
     * @param map attributes i.e. job-id,requesting-user-name,requested-attributes
     * @return ByteBuffer IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(@NonNull URL uri, Map<String, String> map) throws UnsupportedEncodingException {
        IppBuffer ippBuf = new IppBuffer(operationID);

        if (map == null) {
            ippBuf.putUri("job-uri", stripPortNumber(uri));
        } else {
            if (map.containsKey("job-id")) {
                ippBuf.putUri("printer-uri", stripPortNumber(uri));
                int jobId = Integer.parseInt(map.get("job-id"));
                ippBuf.putInteger("job-id", jobId);
            } else {
                ippBuf.putUri("job-uri", stripPortNumber(uri));
            }

            ippBuf.putNameWithoutLanguage("requesting-user-name", map.get("requesting-user-name"));

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
        }
        return ippBuf.getData();
    }

    public PrintJobAttributes getPrintJobAttributes(String hostname, String userName,
                                                    int jobID, CupsAuthentication creds) throws Exception {
        PrintJobAttributes job = null;

        Map<String, String> map = new HashMap<>();
        map.put("requested-attributes", "all");
        map.put("requesting-user-name", userName);
        IppResult result = request(null, new URL("http://" + hostname + "/jobs/" + jobID), map, creds);

        for (AttributeGroup group : result.getAttributeGroupList()) {
            if ("job-attributes-tag".equals(group.getTagName()) || "unassigned".equals(group.getTagName())) {
                if (job == null) {
                    job = new PrintJobAttributes();
                }
                for (Attribute attr : group.getAttributes()) {
                    if (attr.getAttributeValues() != null && !attr.getAttributeValues().isEmpty()) {
                        String attValue = getAttributeValue(attr);

                        String attrName = attr.getName();
                        switch (attrName) {
                            case "job-uri":
                                job.setJobURL(new URL(attValue.replace("ipp://", "http://")));
                                break;
                            case "job-id":
                                job.setJobID(Integer.parseInt(attValue));
                                break;
                            case "job-state":
                                job.setJobState(JobStateEnum.fromString(attValue));
                                break;
                            case "job-printer-uri":
                                job.setPrinterURL(new URL(attValue.replace("ipp://", "http://")));
                                break;
                            case "job-name":
                                job.setJobName(attValue);
                                break;
                            case "job-originating-user-name":
                                job.setUserName(attValue);
                                break;
                            case "job-k-octets":
                                job.setSize(Integer.parseInt(attValue));
                                break;
                            case "time-at-creation":
                                job.setJobCreateTime(new Date(1000 * Long.parseLong(attValue)));
                                break;
                            case "time-at-completed":
                                job.setJobCompleteTime(new Date(1000 * Long.parseLong(attValue)));
                                break;
                            case "job-media-sheets-completed":
                                job.setPagesPrinted(Integer.parseInt(attValue));
                                break;
                        }
                    }
                }
            }
        }
        return job;
    }

}
