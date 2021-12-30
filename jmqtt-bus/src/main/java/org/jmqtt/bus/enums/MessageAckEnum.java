
package org.jmqtt.bus.enums;

public enum MessageAckEnum {

    UN_ACK(0),

    ACK(1),

    ;

    private int code;

    MessageAckEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageAckEnum getByCode(int code) {
        for (MessageAckEnum value : MessageAckEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
