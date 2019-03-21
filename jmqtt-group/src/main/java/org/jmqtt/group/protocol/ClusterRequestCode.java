package org.jmqtt.group.protocol;

/**
 * cluster message request code
 * 0-100
 */
public class ClusterRequestCode {

    /**
     * connect and get other node from the cluster
     */
    public static final int FETCH_NODES = 1;

    /**
     * notice there is a new client
     */
    public static final int NOTICE_NEW_CLIENT = 2;

    /**
     * send message to other jmqtt server
     */
    public static final int SEND_MESSAGE = 4;

    /**
     * transfer session -> subscribe
     */
    public static final int TRANSFER_SESSION = 5;

    /**
     * transfer offline message
     */
    public static final int TRANSFER_SESSION_MESSAGE = 6;

}
