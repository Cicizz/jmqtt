package org.jmqtt.broker.client;

import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.ChannelEventListener;
import org.jmqtt.remoting.netty.NettyConnectHandler;
import org.jmqtt.remoting.netty.NettyEventExcutor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * 客户端生命周期时间钩子执行服务，客户端的各种异常监听在{@link NettyConnectHandler}监听，
 * 随后会把时间放到{@link NettyEventExcutor}中的事件队列中，本服务负责根据各种事件，触发处理逻辑
 * 例如onChannelException会在客户端连接异常时触发，进行关闭设备连接，并移除连接缓存，而当channel关闭时，又触发onChannelClose，进行遗言发送
 */
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
    	String clientId = NettyUtil.getClientId(channel);
    	log.warn("[ClientIdle] -> {} keepAlive timeout ",clientId);
    }

    /**
     * onChannelException会在客户端连接异常时，触发移除连接缓存
     * 注：关闭通道连接已在NettyConnectHandler中的exceptionCaught方法处理
     */
    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
    	String clientId = NettyUtil.getClientId(channel);
		ConnectManager.getInstance().removeClient(clientId);
		log.warn("[ClientLifeCycleHook] -> {} channelException,close channel and remove ConnectCache",clientId);
    }
}
