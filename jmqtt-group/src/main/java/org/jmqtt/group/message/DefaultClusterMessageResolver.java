package org.jmqtt.group.message;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.ClusterMessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClusterMessageResolver implements ClusterMessageResolver {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    public DefaultClusterMessageResolver(){

    }

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
