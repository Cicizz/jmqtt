package org.jmqtt.group.common;

import org.jmqtt.group.protocol.node.ServerNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterNodeManager {

    private final ConcurrentMap<String /* node name */, ServerNode> nodeTable = new ConcurrentHashMap<>();
    private static final ClusterNodeManager INSTANCE = new ClusterNodeManager();
    /**
     * currentNode
     */
    private ServerNode currentNode;

    private ClusterNodeManager(){}

    public static final  ClusterNodeManager getInstance(){
        return INSTANCE;
    }

    public ConcurrentMap<String, ServerNode> getNodeTable() {
        return nodeTable;
    }

    public ServerNode putNewNode(ServerNode node){
        return this.nodeTable.put(node.getNodeName(),node);
    }

    public ServerNode getNode(String nodeName){
        return nodeTable.get(nodeName);
    }

    public ServerNode getCurrentNode() {
        return currentNode;
    }

    public ServerNode removeNode(String nodeName){
        return this.nodeTable.remove(nodeName);
    }

    public void setCurrentNode(ServerNode currentNode) {
        this.currentNode = currentNode;
    }

    public Set<ServerNode> getAllNodes(){
        Collection<ServerNode> allNode = nodeTable.values();
        return new HashSet<>(allNode);
    }

    public Set<ServerNode> getActiveNodes(){
        Set<ServerNode> activeNodes = new HashSet<>();
        Collection<ServerNode> allNode = nodeTable.values();
        for(ServerNode node : allNode){
            if(node.isActive()){
                activeNodes.add(node);
            }
        }
        return activeNodes;
    }
}
