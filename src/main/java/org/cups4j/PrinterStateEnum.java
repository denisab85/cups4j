package org.cups4j;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PrinterStateEnum {
    IDLE(3, "idle"),
    PRINTING(4, "printing"),
    STOPPED(5, "stopped");

    private final Integer value;
    private final String stateName;


    public static PrinterStateEnum fromInteger(Integer value) {
        return value == null ? null :
                Arrays.stream(PrinterStateEnum.values())
                        .filter(printerState -> value.equals(printerState.getValue()))
                        .findFirst()
                        .orElse(null);
    }

    public static PrinterStateEnum fromStringInteger(String value) {
        return value == null ? null :
                Arrays.stream(PrinterStateEnum.values())
                        .filter(printerState -> value.equalsIgnoreCase(printerState.getValue().toString()))
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public String toString() {
        return stateName;
    }

}