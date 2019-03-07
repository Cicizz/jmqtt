package org.jmqtt.group.common;

import java.util.HashMap;

/**
 * cluster remoting command
 */
public class ClusterRemotingCommand {

    /**
     * cluster request code
     */
    private int code;
    private HashMap<String,String> extField;
    private transient byte[] body;

    public ClusterRemotingCommand() {
    }
}
