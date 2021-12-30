package org.jmqtt.bus.enums;


public enum MessageFlowEnum {

    INBOUND("INBOUND"),

    OUTBOUND("OUTBOUND"),

            ;

    private String code;

    MessageFlowEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
