package org.jmqtt.broker.common.model;

public enum AuthorityEnum {

    PUB("pub"),

    SUB("sub"),

    PUB_AND_SUB("pub&sub"),
    ;

    private String code;

    AuthorityEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean hasPub(String code) {
        if (PUB.getCode().equals(code) || PUB_AND_SUB.getCode().equals(code)) {
            return true;
        }
        return false;
    }

    public static boolean hasSub(String code) {
        if (SUB.getCode().equals(code) || PUB_AND_SUB.getCode().equals(code)) {
            return true;
        }
        return false;
    }

}
