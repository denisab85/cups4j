/**
 * Copyright (C) 2009 Harald Weyhing
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.cups4j;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * State of print jobs
 */
@RequiredArgsConstructor
public enum JobStateEnum {
    PENDING("pending"),
    PENDING_HELD("pending-held"),
    PROCESSING("processing"),
    PROCESSING_STOPPED("processing-stopped"),
    CANCELED("canceled"),
    ABORTED("aborted"),
    COMPLETED("completed");

    @Getter
    private final String text;

    public static JobStateEnum fromString(String value) {
        if (value != null) {
            for (JobStateEnum jobState : JobStateEnum.values()) {
                if (value.equalsIgnoreCase(jobState.text)) {
                    return jobState;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return text;
    }

}
