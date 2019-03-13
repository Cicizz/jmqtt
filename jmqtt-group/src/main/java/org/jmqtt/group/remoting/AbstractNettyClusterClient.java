package org.jmqtt.group.remoting;

import io.netty.bootstrap.Bootstrap;

public abstract class AbstractNettyClusterClient extends AbstractNettyCluster {

    public abstract Bootstrap getBootstrap();
}
