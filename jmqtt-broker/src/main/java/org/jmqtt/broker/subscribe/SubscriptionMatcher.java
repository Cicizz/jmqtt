package org.jmqtt.broker.subscribe;

import org.jmqtt.common.bean.Subscription;

import java.util.Set;

/**
 * Subscription tree
 */
public interface SubscriptionMatcher {

    /**
     * add subscribe
     * @param subscription
     * @return  true：new subscribe,dispatcher retain message
     *           false：no need to dispatcher retain message
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
