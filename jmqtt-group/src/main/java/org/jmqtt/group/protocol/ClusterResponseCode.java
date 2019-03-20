package org.jmqtt.group.protocol;

/**
 * cluster message response code
 * 100-~
 */
public class ClusterResponseCode {

    /**
     * response ok
     */
    public static final int RESPONSE_OK = 100;

    /**
     * response error code
     */
    public static final int ERROR_RESPONSE = 101;

    /**
     * request code not supported
     */
    public static final int REQUEST_CODE_NOT_SUPPORTED = 102;

}
