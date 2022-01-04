
package org.jmqtt.mqtt.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.MQTTConnectionFactory;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

public class NettyMqttHandler extends ChannelDuplexHandler {

    private static final Logger  log = JmqttLogger.remotingLog;

    private MQTTConnectionFactory connectionFactory;

    NettyMqttHandler(MQTTConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        MqttMessage msg = MqttNettyUtils.validateMessage(message);
        final MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        try {
            mqttConnection.processProtocol(ctx,msg);
        } catch (Throwable ex) {
            LogUtil.error(log,"Error processing protocol message: {}", msg.fixedHeader().messageType(), ex);
            ctx.channel().close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    LogUtil.info(log,"Closed client channel due to exception in processing");
                }
            });
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        MQTTConnection connection = connectionFactory.create(ctx.channel());
        MqttNettyUtils.mqttConnection(ctx.channel(), connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        mqttConnection.handleConnectionLost();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogUtil.error(log,"Unexpected exception while processing MQTT message. Closing Netty channel. CId={}",
                MqttNettyUtils.clientID(ctx.channel()), cause);
        ctx.close().addListener(CLOSE_ON_FAILURE);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        //if (evt instanceof InflightResender.ResendNotAckedPublishes) {
        //    final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
        //    mqttConnection.resendNotAckedPublishes();
        //}
        ctx.fireUserEventTriggered(evt);
    }

}
