package com.kalus.tdengineorm.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Klaus
 */
public enum TdLogLevelEnum {
    DEBUG, INFO, WARN, ERROR;

    @JsonValue
    public static TdLogLevelEnum match(String logLevelStr) {
        for (TdLogLevelEnum tdLogLevelEnum : values()) {
            if (tdLogLevelEnum.name().equalsIgnoreCase(logLevelStr)) {
                return tdLogLevelEnum;
            }
        }
        return null;
    }
}
