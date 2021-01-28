package org.jmqtt.broker.store;

/**
 * 集群消息事件码
 */
public enum ClusterEvent {

    CLEAR_SESSION("CLEAR_SESSION","清理本节点客户端会话缓存"),

    DISPATCHER_CLIENT_MESSAGE("DISPATCHER_CLIENT_MESSAGE","向集群分发客户端发送的消息"),

    DISPATCHER_WILL_MESSAGE("DISPATCHER_WILL_MESSAGE","向集群分发will消息"),

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
