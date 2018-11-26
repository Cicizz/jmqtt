package org.jmqtt.remoting.auth;

public interface IAuthentication {


    /**
     * valide username and password is allowed
     * @param clientId
     * @param username
     * @param password
     * @return
     */
    boolean canConnect(String clientId,String username,String password);

}
