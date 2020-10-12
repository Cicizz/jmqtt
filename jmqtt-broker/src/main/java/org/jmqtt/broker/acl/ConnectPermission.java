package org.jmqtt.broker.acl;

/**
 * Connect permission manager
 */
public interface ConnectPermission {

    /**
     * verify the clientId whether it meets the requirements or not
     */
    boolean clientIdVerify(String clientId);

    /**
     * if the client is on blacklist,is not allowed to connect
     */
    boolean onBlacklist(String remoteAddr,String clientId);

    /**
     * verify the clientId,username,password whether true or not
     */
    boolean authentication(String clientId,String userName,byte[] password);

    /**
     * verify the client's heartbeat time whether the compliance
     */
    boolean verifyHeartbeatTime(String clientId,int time);
}
