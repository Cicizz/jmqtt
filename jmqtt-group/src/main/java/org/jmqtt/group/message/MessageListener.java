package org.jmqtt.group.message;

import org.jmqtt.group.protocol.ClusterRemotingCommand;

public interface MessageListener {

    ReceiveMessageStatus receive(ClusterRemotingCommand clusterRemotingCommand);
}
