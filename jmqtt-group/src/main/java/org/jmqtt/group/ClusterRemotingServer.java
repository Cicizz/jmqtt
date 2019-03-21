package org.jmqtt.group;

import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.remoting.RemotingService;

import java.util.concurrent.ExecutorService;

/**
 * receive request and handle some service
 */
public interface ClusterRemotingServer extends RemotingService {

    void registerClusterProcessor(int code, ClusterRequestProcessor processor, ExecutorService executorService);
}
