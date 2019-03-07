package org.jmqtt.group;

/**
 * connect to other cluster server and send request
 */
public interface ClusterClient {

    /**
     * start clusterClient
     */
    void start();

    /**
     * shutdown clusterClient
     */
    void shutdown();
}
