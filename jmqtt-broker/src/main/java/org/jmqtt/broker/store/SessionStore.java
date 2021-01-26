
package org.jmqtt.broker.store;

import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;

import java.util.Collection;
import java.util.Set;

/**
 * 存储客户端会话信息
 *  1. 会话是否存在, 即使会话状态其余部分为空.
 *  2. 客户端订阅信息, 包括任何订阅标识符.
 *  3. 已发送给客户端, 但是还没有完成确认的QoS等级1和QoS等级2的消息.
 *  4. 等待传输给客户端的QoS等级0(可选), QoS等级1和QoS等级2的消息.
 *  5. 从客户端收到的, 但是还没有完成确认的QoS等级2消息. 遗嘱小子和遗嘱延时间隔.
 *  6. 如果会话当前未连接, 会话结束时间和会话状态将被丢弃.
 *
 *  TODO 待实现
 */
public interface SessionStore {

    /**
     * 从集群中查询该clientId之前的连接状态
     */
    SessionState getSession(String clientId);

    /**
     * 1. 保存会话到 Jmqtt集群
     * 2. 通知集群其它服务器，把该连接的本地会话信息清理掉
     */
    boolean storeSession(String clientId,SessionState sessionState,boolean notifyClearOtherSession);

    /**
     * 清理会话信息：
     *  1. 入栈出栈中的过程消息
     *  2. 离线消息
     *  3. 订阅关系
     *  4. 订阅状态{@link SessionState}
     */
    void clearSession(String clientId);

    /**
     * 存储订阅关系
     */
    boolean storeSubscription(String clientId,Subscription subscription);

    /**
     * 移除订阅关系
     */
    boolean delSubscription(String clientId,String topic);

    /**
     * 获取该clientId的所有的订阅关系
     */
    Set<Subscription> getSubscriptions(String clientId);

    /**
     * 缓存qos2 publish报文消息-入栈消息
     * @return true:缓存成功   false:缓存失败
     */
    boolean cacheInflowMsg(String clientId, Message message);

    /**
     * 获取并删除接收到的qos2消息-入栈消息
     * @return
     */
    Message releaseInflowMsg(String clientId,int msgId);

    /**
     * 获取所有的入栈消息
     */
    Collection<Message> getAllInflowMsg(String clientId);

    /**
     * 缓存出栈消息-分发给客户端的qos1,qos2消息
     */
    boolean cacheOutflowMsg(String clientId,Message message);

    /**
     * 是否包含该出栈消息
     */
    boolean containOutflowMsg(String clientId,int msgId);

    /**
     * 获取所有的出栈消息
     */
    Collection<Message> getAllOutflowMsg(String clientId);

    /**
     * 获取并删除发送的出栈消息
     * @param clientId
     * @param msgId
     * @return
     */
    Message releaseOutflowMsg(String clientId,int msgId);

    /**
     * 缓存离线消息
     */
    boolean storeOfflineMsg(String clientId,Message message);

    /**
     * 获取所有的离线消息
     */
    Collection<Message> getAllOfflineMsg(String clientId);

    /**
     * 清理该客户端的离线消息
     */
    boolean clearOfflineMsg(String clientId);
}