package org.jmqtt.group.common;

/**
 * message code
 */
public class ClusterRequestCode {

    /**
     * notice there is a new
     */
    public static final int NOTIC_NEW_CLIENT = 1;

    /**
     * notice other jmqtt server return this topic's retain message
     * if the subscribe is a new subscription
     */
    public static final int NOTICE_NEW_SUBSCRIPTION = 2;

    /**
     * send message to other jmqtt server
     */
    public static final int SEND_MESSAGE = 3;
}
