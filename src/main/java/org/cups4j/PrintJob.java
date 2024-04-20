package org.cups4j;

/**
 * Copyright (C) 2009 Harald Weyhing
 * <p>
 * This file is part of Cups4J. Cups4J is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * Cups4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with Cups4J. If
 * not, see <http://www.gnu.org/licenses/>.
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Builder
public class PrintJob {
    @Getter
    private final InputStream document;
    @Getter
    private final int copies;
    @Getter
    private final String pageRanges;
    @Getter
    private final String jobName;
    @Getter
    private final String pageFormat;
    @Getter
    private final String resolution;
    @Getter
    @Builder.Default
    private String userName = CupsClient.DEFAULT_USER;
    @Getter
    private boolean duplex;
    @Getter
    @Builder.Default
    private boolean portrait = true;
    @Getter
    private boolean color;
    /**
     * Additional attributes for the print operation and the print job
     * <p>
     * provide operation attributes and/or a String of job-attributes
     * <p>
     * job attributes are separated by "#"
     * </p>
     *
     * <p>
     * example:
     * </p>
     * <p>
     * attributes.put("compression","none");
     * </p>
     * <p>
     * attributes.put("job-attributes",
     * "print-quality:enum:3#sheet-collate:keyword:collated#sides:keyword:two-sided-long-edge"
     * );
     * </p>
     * <p>
     * take a look config/ippclient/list-of-attributes.xml for more
     * information
     * </p>
     */
    @Setter
    private Map<String, String> attributes;

    public Map<String, String> getAttributes() {
        return attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "-" + getJobName();
    }

}
