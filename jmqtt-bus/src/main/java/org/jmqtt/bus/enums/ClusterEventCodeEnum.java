package org.jmqtt.bus.enums;

/**
 * cluster event code
 */
public enum ClusterEventCodeEnum {

    CLEAR_SESSION("CLEAR_SESSION"),

    DISPATCHER_CLIENT_MESSAGE("DISPATCHER_CLIENT_MESSAGE"),
    ;

    private String    code;

    ClusterEventCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
