package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.helper.RejectHandler;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.ClusterRemotingClient;
import org.jmqtt.group.ClusterRemotingServer;
import org.jmqtt.group.MessageTransfer;
import org.jmqtt.group.common.ClusterNodeManager;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.common.ResponseFuture;
import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.group.processor.SendMessageProcessor;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.jmqtt.group.protocol.node.ServerNode;
import org.jmqtt.remoting.exception.RemotingConnectException;
import org.jmqtt.remoting.exception.RemotingSendRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultMessageTransfer implements MessageTransfer {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private ClusterRemotingClient clusterRemotingClient;
    private ClusterRemotingServer clusterRemotingServer;
    private MessageListener messageListener;
    private ExecutorService messageService;

    public DefaultMessageTransfer(ClusterRemotingClient clusterRemotingClient,ClusterRemotingServer clusterRemotingServer){
        this.clusterRemotingClient = clusterRemotingClient;
        this.clusterRemotingServer = clusterRemotingServer;
    }

    @Override
    public void send(ClusterRemotingCommand message) {
        Set<ServerNode> activeNodes = ClusterNodeManager.getInstance().getActiveNodes();
        if(activeNodes == null || activeNodes.size() == 0){
            log.debug("send message to cluster:there is no other active node");
            return;
        }
        long timeoutMillis = 3000;
        for (ServerNode node : activeNodes){
            String addr = node.getAddr();
            try {
                this.clusterRemotingClient.invokeAsync(addr, message, timeoutMillis, new InvokeCallback() {
                    @Override
                    public void invokeComplete(ResponseFuture responseFuture) {
                        log.debug("send message success,nodeName={},addr={},code={}",node.getNodeName(),node.getAddr(),message.getCode());
                    }
                });
            } catch (RemotingConnectException e) {
                log.warn("Send message to cluster connect exception,nodeName={},addr={},code={},ex={}",node.getNodeName(),addr,message.getCode(),e);
            } catch (RemotingSendRequestException e) {
                log.warn("Send message to cluster request exception,nodeName={},addr={},code={},ex={}",node.getNodeName(),addr,message.getCode(),e);
            }
        }
    }

    @Override
    public void send(String nodeName, ClusterRemotingCommand message) {
        long timeoutMillis = 3000;
        ServerNode node = ClusterNodeManager.getInstance().getNode(nodeName);
        String addr = node.getAddr();
        try {
            this.clusterRemotingClient.invokeAsync(addr, message, timeoutMillis, new InvokeCallback() {
                @Override
                public void invokeComplete(ResponseFuture responseFuture) {
                    log.debug("send message success,nodeName={},addr={},code={}",nodeName,node.getAddr(),message.getCode());
                }
            });
        } catch (RemotingConnectException e) {
            log.warn("Send message to cluster connect exception,nodeName={},addr={},code={},ex={}",nodeName,addr,message.getCode(),e);
        } catch (RemotingSendRequestException e) {
            log.warn("Send message to cluster request exception,nodeName={},addr={},code={},ex={}",nodeName,addr,message.getCode(),e);
        }
    }

    @Override
    public void registerListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        registerProcessorWithDefault();
    }

    private void registerProcessorWithDefault(){
        this.messageService =  new ThreadPoolExecutor(8,
                8,
                60000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new ThreadFactoryImpl("ClusterThread"),
                new RejectHandler("sub", 100000));
        ClusterRequestProcessor sendMessageProcessor = new SendMessageProcessor(messageListener);
        this.clusterRemotingServer.registerClusterProcessor(ClusterRequestCode.NOTICE_NEW_CLIENT,sendMessageProcessor,messageService);
        this.clusterRemotingServer.registerClusterProcessor(ClusterRequestCode.SEND_MESSAGE,sendMessageProcessor,messageService);
    }

}
