package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisconnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);
    private WillMessageStore willMessageStore;

    public DisconnectProcessor(WillMessageStore willMessageStore){
        this.willMessageStore = willMessageStore;
    }
    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        if(!ConnectManager.getInstance().containClient(clientId)){
            log.warn("[DISCONNECT] -> {} hasn't connect before",clientId);
        }
        ConnectManager.getInstance().removeClient(clientId);
        removeWill(clientId);
        ctx.close();
    }

    private void removeWill(String clientId){
        if(willMessageStore.hasWillMessage(clientId)){
            willMessageStore.removeWillMessage(clientId);
        }
    }


}
