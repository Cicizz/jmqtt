package org.jmqtt.bus.subscription;

import org.jmqtt.bus.subscription.model.Subscription;

import java.util.Set;

/**
 * Subscription tree
 * 订阅树处理
 *  TODO 1.支持共享订阅
 *  TODO 2.支持 p2p消息(jmqtt 特有)
 *  TODO 2.支持系统topic $SYS -> 需要定时采集本机节点等信息下发
 */
public interface SubscriptionMatcher {

    /**
     * add subscribe
     * @return  true：new subscribe,dispatcher retain message
     *           false：no need to dispatcher retain message
     */
    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic,String clientId);

    /**
     * 获取匹配该topic下的非共享订阅者
     */
    Set<Subscription> match(String topic);


    int size();

    String dumpTree();

}
