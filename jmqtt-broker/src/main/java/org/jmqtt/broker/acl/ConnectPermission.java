package org.jmqtt.broker.acl;

/**
 * Connect permission manager
 */
public interface ConnectPermission {

    /**
     * Verfy the clientId whether it meets the requirements or not
     */
    boolean clientIdVerfy(String clientId);

    /**
     * if the client is on blacklist,is not allowed to connect
     */
    boolean onBlacklist(String remoteAddr,String clientId);

    /**
     * verfy the clientId,username,password whether true or not
     */
    boolean authentication(String clientId,String userName,byte[] password);

    /**
     * verfy the client's heartbeat time whether the compliance
     */
    boolean verfyHeartbeatTime(String clientId,int time);
}
