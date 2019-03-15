package org.jmqtt.group.protocol;

/**
 * message code
 */
public class ClusterRequestCode {

    /**
     * connect and get other node from the cluster
     */
    public static final int FETCH_NODES = 1;

    /**
     * notice there is a new client
     */
    public static final int NOTIC_NEW_CLIENT = 2;

    /**
     * notice other jmqtt server return this topic's retain message
     * if the subscribe is a new subscription
     */
    public static final int NOTICE_NEW_SUBSCRIPTION = 3;

    /**
     * send message to other jmqtt server
     */
    public static final int SEND_MESSAGE = 4;

    /**
     * response error code
     */
    public static final int ERROR_RESPONSE = 100;
    /**
     * request code not supported
     */
    public static final int REQUEST_CODE_NOT_SUPPORTED = 101;
}
