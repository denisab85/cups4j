package org.cups4j;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrinterStateEnum {
    IDLE(3, "idle"),
    PRINTING(4, "printing"),
    STOPPED(5, "stopped");

    private final Integer value;
    private final String stateName;


    public static PrinterStateEnum fromInteger(Integer value) {
        if (value != null) {
            for (PrinterStateEnum printerState : PrinterStateEnum.values()) {
                if (value.equals(printerState.getValue())) {
                    return printerState;
                }
            }
        }
        return null;
    }

    public static PrinterStateEnum fromStringInteger(String value) {
        if (value != null) {
            for (PrinterStateEnum printerState : PrinterStateEnum.values()) {
                if (value.equalsIgnoreCase(printerState.getValue().toString())) {
                    return printerState;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return stateName;
    }

}