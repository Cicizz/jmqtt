package org.jmqtt.broker.client;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.ChannelEventListener;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLifeCycleHookService implements ChannelEventListener {

	private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);
    private WillMessageStore willMessageStore;
    private MessageDispatcher messageDispatcher;

    public ClientLifeCycleHookService(WillMessageStore willMessageStore,MessageDispatcher messageDispatcher){
        this.willMessageStore = willMessageStore;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        String clientId = NettyUtil.getClientId(channel);
        if(StringUtils.isNotEmpty(clientId)){
            if(willMessageStore.hasWillMessage(clientId)){
                Message willMessage = willMessageStore.getWillMessage(clientId);
                messageDispatcher.appendMessage(willMessage);
            }
        }
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
    	String clientId = NettyUtil.getClientId(channel);
		ConnectManager.getInstance().removeClient(clientId);
		log.warn("[ClientLifeCycleHook] -> {} channelException,close channel and remove ConnectCache!",clientId);
    }
}
