package org.jmqtt.bus;


public interface Authenticator {


    boolean login(String clientId,String userName,byte[] password);

    boolean onBlackList(String clientId,String remoteIpAddress);

    boolean clientIdVerify(String clientId);

    boolean subscribeVerify(String clientId,String topic);
}
