
package org.jmqtt.bus.enums;


public enum TransportProtocolEnum {

    MQTT("MQTT"),

    TCP("TCP"),

    COAP("COAP"),
    ;

    private String code;

    TransportProtocolEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
