
package org.jmqtt.broker.cluster;

import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.broker.dispatcher.MessageDispatcher;

public class DefaultClusterMessageTransfer extends ClusterMessageTransfer {

    public DefaultClusterMessageTransfer(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public CommandReqOrResp sendMessage(CommandReqOrResp commandReqOrResp) {
        return null;
    }
}