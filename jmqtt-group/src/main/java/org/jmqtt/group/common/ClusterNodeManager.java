package org.jmqtt.group.common;

import org.jmqtt.group.protocol.ServerNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterNodeManager {

    private ConcurrentMap<String /* node ipAddr */, ServerNode> nodeTable = new ConcurrentHashMap<>();
    private ClusterNodeManager INSTANCE = new ClusterNodeManager();
    /**
     * currentNode
     */
    private ServerNode currentNode;

    private ClusterNodeManager(){}

    public ClusterNodeManager getINSTANCE(){
        return this.INSTANCE;
    }

    public ConcurrentMap<String, ServerNode> getNodeTable() {
        return nodeTable;
    }

    public void setNodeTable(ConcurrentMap<String, ServerNode> nodeTable) {
        this.nodeTable = nodeTable;
    }

    public void setINSTANCE(ClusterNodeManager INSTANCE) {
        this.INSTANCE = INSTANCE;
    }

    public ServerNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(ServerNode currentNode) {
        this.currentNode = currentNode;
    }
}
