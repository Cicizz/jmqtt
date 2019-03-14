package org.jmqtt.group;

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
     * receive message from cluster
     */
    ClusterRemotingCommand receive();
}
