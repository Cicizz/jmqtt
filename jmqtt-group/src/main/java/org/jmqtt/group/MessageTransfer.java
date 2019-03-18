package org.jmqtt.group;

import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.protocol.ClusterRemotingCommand;

/**
 * cluster message transfer
 */
public interface MessageTransfer {

    /**
     * send message to cluster
     */
    void send(ClusterRemotingCommand message);

    /**
     * message listener: receive message from cluster
     */
    void registerListener(MessageListener messageListener);
}
