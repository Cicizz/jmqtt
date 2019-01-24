package org.jmqtt.group.message;

import org.jmqtt.common.bean.Message;
import org.jmqtt.group.ClusterMessageResolver;

public class DefaultClusterMessageResolver implements ClusterMessageResolver {

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void send(Message message) {

    }

    @Override
    public Message receive() {
        return null;
    }
}
