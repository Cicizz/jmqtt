package org.jmqtt.group.common;

import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.protocol.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterNodeManager {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private ConcurrentMap<String /* node ipAddr */, ServerNode> nodeTable = new ConcurrentHashMap<>();
    private ClusterNodeManager INSTANCE = new ClusterNodeManager();

    private ClusterNodeManager(){}

}
