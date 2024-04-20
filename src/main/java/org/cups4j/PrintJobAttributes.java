package org.cups4j;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * Holds print job attributes
 */
@Slf4j
@Setter
@Getter
public class PrintJobAttributes {

    private URL jobURL = null;
    private URL printerURL = null;
    private int jobID = -1;
    private JobStateEnum jobState = null;
    private String jobName = null;
    private String userName = null;
    private Date jobCreateTime;
    private Date jobCompleteTime;
    private int pagesPrinted = 0;
    // Size of the job in kb (this value is rounded up by the IPP server)
    // This value is optional and might not be reported by your IPP server
    private int size = -1;

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);

    @Override
    public String toString() {
        String buff = "job name/job id : [" + getJobName() + "/" + getJobID() + "]\n" +
                "user name : [" + getUserName() + "]\n" +
                "job state : [" + getJobState() + "]\n" +
                "job URL : [" + getJobURL() + "]\n" +
                "printer URL : [" + getPrinterURL() + "]\n" +
                "job size/pages printed : [" + getSize() + "kB/" + getPagesPrinted() + "]\n";
        return buff;
    }

    public URL getJobURL(CupsClient client) throws Exception {
        return client.getJobAttributes(getJobID()).getJobURL();
    }

    public int getPagesPrinted(CupsClient client) throws Exception {
        return client.getJobAttributes(getJobID()).getPagesPrinted();
    }

    public int getSize(CupsClient client) throws Exception {
        return client.getJobAttributes(getJobID()).getSize();
    }

    public String getCreateDate(CupsClient client) throws Exception {
        return this.dateFormat.format(client.getJobAttributes(getJobID()).getJobCreateTime());
    }

    public String getCompleteDate(CupsClient client) throws Exception {
        return this.dateFormat.format(client.getJobAttributes(getJobID()).getJobCompleteTime());
    }

    public String toString(CupsClient client) {
        try {
            StringBuilder buff = new StringBuilder(toString());

            Date createDate;
            Date completeDate;

            if (client != null) {
                createDate = client.getJobAttributes(getJobID()).getJobCreateTime();
                completeDate = client.getJobAttributes(getJobID()).getJobCompleteTime();

                buff.append("job creation time : [").append(dateFormat.format(createDate.getTime())).append("]\n");
                buff.append("job completion time : [").append(dateFormat.format(completeDate.getTime())).append("]\n");
            }

            return buff.toString();
        } catch (Exception ex) {
            log.error("Unable to get creation and/or completion time for job " + getJobID(), ex);
            return toString();
        }

    }
}
