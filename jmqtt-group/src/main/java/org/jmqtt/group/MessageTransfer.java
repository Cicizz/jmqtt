package org.jmqtt.group;

import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.protocol.ClusterRemotingCommand;

/**
 * cluster message transfer
 */
public interface MessageTransfer {

    /**
     * broadcast send message to cluster
     */
    void send(ClusterRemotingCommand message);

    /**
     * send message to the specified node
     * @param nodeName
     * @param message
     */
    void send(String nodeName,ClusterRemotingCommand message);

    /**
     * message listener: receive message from cluster
     */
    void registerListener(MessageListener messageListener);
}
