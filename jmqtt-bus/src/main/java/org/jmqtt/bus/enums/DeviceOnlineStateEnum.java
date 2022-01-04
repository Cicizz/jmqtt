
package org.jmqtt.bus.enums;

public enum DeviceOnlineStateEnum {

    ONLINE("ONLINE"),

    OFFLINE("OFFLINE"),

    ;

    private String code;

    DeviceOnlineStateEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
