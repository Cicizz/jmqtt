package org.jmqtt.broker.processor.dispatcher.redis;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;

import java.util.List;

public class RedisClusterEventHandler implements ClusterEventHandler {
    @Override
    public void start(BrokerConfig brokerConfig) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean sendEvent(Event event) {
        return false;
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {

    }

    @Override
    public List<Event> pollEvent(int maxPollNum) {
        return null;
    }
}
