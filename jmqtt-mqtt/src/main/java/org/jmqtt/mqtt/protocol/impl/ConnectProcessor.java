package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.mqtt.utils.MqttMessageUtil;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingHelper;
import org.slf4j.Logger;

/**
 * mqtt 客户端连接逻辑处理 强制约束：jmqtt在未返回conAck之前，不接收其它任何mqtt协议报文（mqtt协议可以允许） TODO mqtt5 协议支持
 */
public class ConnectProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.mqttLog;

    @Override
    public void processRequest(ChannelHandlerContext ctx,MqttMessage mqttMessage) {
        MqttConnectMessage connectMessage = (MqttConnectMessage) mqttMessage;
        MqttConnectReturnCode returnCode = null;
        int mqttVersion = connectMessage.variableHeader().version();
        String clientId = connectMessage.payload().clientIdentifier();
        String userName = connectMessage.payload().userName();
        byte[] password = connectMessage.payload().passwordInBytes();
        boolean cleanSession = connectMessage.variableHeader().isCleanSession();
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        boolean sessionPresent = false;
        try {

            if (!versionValid(mqttVersion)) {
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
            } else if (!mqttConnection.clientIdVerify(clientId)) {
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            } else if (mqttConnection.onBlackList(RemotingHelper.getRemoteAddr(ctx.channel()), clientId)) {
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
            } else if (!mqttConnection.login(clientId, userName, password)) {
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
            } else {
                // 1. 设置心跳
                int heartbeatSec = connectMessage.variableHeader().keepAliveTimeSeconds();
                if (!mqttConnection.keepAlive(heartbeatSec)) {
                    LogUtil.warn(log,"[CONNECT] -> set heartbeat failure,clientId:{},heartbeatSec:{}", clientId, heartbeatSec);
                    throw new Exception("set heartbeat failure");
                }
                // 2. 处理连接
                sessionPresent = mqttConnection.createOrReopenSession(connectMessage);

                returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
                MqttNettyUtils.clientID(ctx.channel(), clientId);
            }

            if (returnCode != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                ctx.close();
                LogUtil.warn(log,"[CONNECT] -> {} connect failure,returnCode={}", clientId, returnCode);
            }
            MqttConnAckMessage ackMessage = MqttMessageUtil.getConnectAckMessage(returnCode, sessionPresent);

            final boolean hasOldSession = sessionPresent;
            ctx.writeAndFlush(ackMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    LogUtil.info(log,"[CONNECT] -> {} connect to this mqtt server", clientId);
                    if (future.isSuccess()) {
                        // store session
                        mqttConnection.storeSession();
                        // send unAckMessages
                        if (!cleanSession && hasOldSession) {
                            mqttConnection.reSendMessage2Client();
                        }
                    } else {
                        LogUtil.error(log,"[CONNECT] -> send connect ack error.");
                        mqttConnection.abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
                    }
                }
            });
            //LogUtil.warn(log,"Connect Cost:{}",(System.currentTimeMillis() - start));
        } catch (Exception ex) {
            LogUtil.warn(log,"[CONNECT] -> Service Unavailable: cause={}", ex);
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
            MqttConnAckMessage ackMessage = MqttMessageUtil.getConnectAckMessage(returnCode, sessionPresent);
            ctx.writeAndFlush(ackMessage);
            ctx.close();
        }
    }

    private boolean versionValid(int mqttVersion) {
        if (mqttVersion == 3 || mqttVersion == 4) {
            return true;
        }
        return false;
    }

}
