package org.jmqtt.broker.subscribe;

import org.jmqtt.common.bean.Subscription;

import java.util.Set;

/**
 * Subscription tree
 */
public interface SubscriptionMatcher {

    /**
     * 添加订阅到订阅树中
     * @param topic 订阅的Topic
     * @param subscription 订阅对象
     * @return  true：新增的订阅，无该topic存在或者qos不同，那么必须分发retain消息
     *           false：重复订阅或订阅异常,不分发retain消息
     */
    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic,String clientId);

    Set<Subscription> match(String topic);

    /**
     *
     * @param pubTopic
     * @param subTopic
     * @return
     */
    boolean isMatch(String pubTopic,String subTopic);
}
