package org.jmqtt.group;

/**
 * receive request and handle some service
 */
public interface ClusterServer {

    /**
     * start cluster server
     */
    void start();

    /**
     * shutdow cluster server
     */
    void shutdown();
}
