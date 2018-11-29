package org.jmqtt.broker.subscribe;

import java.util.List;

public interface SubscriptionMatcher {

    boolean subscribe(String topic,String clientId);

    List<String> match(String topic);
}
