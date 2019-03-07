package org.jmqtt.group.remoting;

import io.netty.bootstrap.Bootstrap;
import org.jmqtt.group.ClusterClient;

public abstract class AbstractNettyClusterClient implements ClusterClient {

    public abstract Bootstrap getBootstrap();
}
