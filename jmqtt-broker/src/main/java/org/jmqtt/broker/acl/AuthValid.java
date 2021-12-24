package org.jmqtt.broker.acl;

import org.jmqtt.broker.common.config.BrokerConfig;

/**
 * Connect permission manager
 */
public interface AuthValid {

    void start(BrokerConfig brokerConfig);

    void shutdown();

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

    /**
     * verify the clientId whether can publish message to the topic
     */
    boolean publishVerify(String clientId,String topic);

    /**
     * verify the clientId whether can subscribe the topic
     */
    boolean subscribeVerify(String clientId,String topic);
}
