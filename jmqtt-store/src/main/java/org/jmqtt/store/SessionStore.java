package org.jmqtt.store;

/**
 * 保存会话信息
 */
public interface SessionStore {

    /**
     * 是否包含该会话信息
     */
    boolean containSession(String clientId);

    /**
     * 设置session状态
     */
    Object setSession(String clientId,Object obj);

    /**
     * 获取上次的会话信息,cleansession=false时，需要重新加载
     * 若为true：表示该客户端在线，需要挤掉之前连接的客户端
     * 若为空：表示从未连接过
     * 若为字符串：该字符串表示上次离线时间
     */
    Object getLastSession(String clientId);

    /**
     * 清除会话信息
     */
    boolean clearSession(String clientId);
}
