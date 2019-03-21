package org.jmqtt.group;

import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.remoting.RemotingService;
import org.jmqtt.remoting.exception.RemotingConnectException;
import org.jmqtt.remoting.exception.RemotingSendRequestException;

/**
 * connect to other cluster server and send request
 */
public interface ClusterRemotingClient extends RemotingService {

    void invokeAsync(final String addr, final ClusterRemotingCommand command, final long timeoutMills, InvokeCallback invokeCallback) throws RemotingConnectException, RemotingSendRequestException;
}
