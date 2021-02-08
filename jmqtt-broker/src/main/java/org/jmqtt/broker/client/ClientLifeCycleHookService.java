package org.jmqtt.broker.client;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.processor.dispatcher.InnerMessageDispatcher;
import org.jmqtt.broker.remoting.netty.ChannelEventListener;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.MessageStore;
import org.slf4j.Logger;

public class ClientLifeCycleHookService implements ChannelEventListener {

    private static final Logger                 log = JmqttLogger.clientTraceLog;
    private              MessageStore           messageStore;
    private              InnerMessageDispatcher innerMessageDispatcher;

    public ClientLifeCycleHookService(MessageStore messageStore, InnerMessageDispatcher innerMessageDispatcher) {
        this.messageStore = messageStore;
        this.innerMessageDispatcher = innerMessageDispatcher;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        String clientId = NettyUtil.getClientId(channel);
        if (StringUtils.isNotEmpty(clientId)) {
            Message willMessage = messageStore.getWillMessage(clientId);
            if (willMessage != null) {
                innerMessageDispatcher.appendMessage(willMessage);
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
        LogUtil.warn(log, "[ClientLifeCycleHook] -> {} channelException,close channel and remove ConnectCache!", clientId);
    }
}
