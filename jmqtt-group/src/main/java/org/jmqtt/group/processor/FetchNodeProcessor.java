package org.jmqtt.group.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.common.ClusterNodeManager;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.jmqtt.group.protocol.ClusterResponseCode;
import org.jmqtt.group.protocol.node.ServerNode;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class FetchNodeProcessor implements ClusterRequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    public FetchNodeProcessor() {
    }

    @Override
    public ClusterRemotingCommand processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        byte[] body = cmd.getBody();
        ServerNode serverNode = SerializeHelper.deserialize(body, ServerNode.class);
        if (serverNode != null) {
            serverNode.setLastUpdateTime(System.currentTimeMillis());
            serverNode.setActive(true);
            ServerNode prevNode = ClusterNodeManager.getInstance().putNewNode(serverNode);
            if (prevNode != null) {
                log.info("fetch node request prev node is not null,nodeName:{},nodeAddr:{},nodeActive:{}", prevNode.getNodeName(), prevNode.getAddr(), prevNode.isActive());
            }
            Set<ServerNode> ownActiveNodes = ClusterNodeManager.getInstance().getActiveNodes();
            ClusterRemotingCommand responseCommand = new ClusterRemotingCommand(ClusterRequestCode.FETCH_NODES);
            responseCommand.setBody(SerializeHelper.serialize(ownActiveNodes));
            return responseCommand;
        } else {
            log.warn("remote node is null,addr={}", RemotingHelper.getRemoteAddr(ctx.channel()));
        }
        ClusterRemotingCommand responseCommand = new ClusterRemotingCommand(ClusterResponseCode.ERROR_RESPONSE);
        return responseCommand;
    }
}
