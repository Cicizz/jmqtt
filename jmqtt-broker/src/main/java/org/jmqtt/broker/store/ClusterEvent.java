package org.jmqtt.broker.store;

/**
 * 集群消息事件码
 */
public enum ClusterEvent {

    CLEAR_SESSION("CLEAR_SESSION","清理本节点客户端会话缓存"),

    ;

    private String code;
    private String desc;

    ClusterEvent(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
