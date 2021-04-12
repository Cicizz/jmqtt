package org.jmqtt.broker.subscribe;

import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;

public interface GroupSubscriptionAndMessageListener {

    void subscribe(Subscription subscription);

    void unSubscribe(String topic, String clientId);

    void receiveNewMessage(Message message);


}
