package org.jmqtt.mqtt.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.jmqtt.mqtt.MQTTConnection;

import java.io.IOException;

public class MqttNettyUtils {

    public static final String ATTR_USERNAME = "username";
    private static final String ATTR_CLIENTID = "ClientID";
    private static final String CLEAN_SESSION = "removeTemporaryQoS2";
    private static final String KEEP_ALIVE = "keepAlive";
    private static final String               CONNECTION     = "connection";
    private static final AttributeKey<Object> ATTR_KEY_CONNECTION = AttributeKey.valueOf(CONNECTION);
    private static final AttributeKey<Object> ATTR_KEY_KEEPALIVE = AttributeKey.valueOf(KEEP_ALIVE);
    private static final AttributeKey<Object> ATTR_KEY_CLEANSESSION = AttributeKey.valueOf(CLEAN_SESSION);
    private static final AttributeKey<Object> ATTR_KEY_CLIENTID = AttributeKey.valueOf(ATTR_CLIENTID);
    private static final AttributeKey<Object> ATTR_KEY_USERNAME = AttributeKey.valueOf(ATTR_USERNAME);

    public static Object getAttribute(ChannelHandlerContext ctx, AttributeKey<Object> key) {
        Attribute<Object> attr = ctx.channel().attr(key);
        return attr.get();
    }

    public static void keepAlive(Channel channel, int keepAlive) {
        channel.attr(ATTR_KEY_KEEPALIVE).set(keepAlive);
    }

    public static void cleanSession(Channel channel, boolean cleanSession) {
        channel.attr(ATTR_KEY_CLEANSESSION).set(cleanSession);
    }

    public static boolean cleanSession(Channel channel) {
        return (Boolean) channel.attr(ATTR_KEY_CLEANSESSION).get();
    }

    public static void clientID(Channel channel, String clientID) {
        channel.attr(ATTR_KEY_CLIENTID).set(clientID);
    }

    public static String clientID(Channel channel) {
        return (String) channel.attr(ATTR_KEY_CLIENTID).get();
    }


    public static void mqttConnection(Channel channel, MQTTConnection mqttConnection) {
        channel.attr(ATTR_KEY_CONNECTION).set(mqttConnection);
    }

    public static MQTTConnection mqttConnection(Channel channel) {
        return (MQTTConnection) channel.attr(ATTR_KEY_CONNECTION).get();
    }

    public static void userName(Channel channel, String username) {
        channel.attr(ATTR_KEY_USERNAME).set(username);
    }

    public static String userName(Channel channel) {
        return (String) channel.attr(ATTR_KEY_USERNAME).get();
    }

    public static MqttMessage validateMessage(Object message) throws IOException, ClassCastException {
        MqttMessage msg = (MqttMessage) message;
        if (msg.decoderResult() != null && msg.decoderResult().isFailure()) {
            throw new IOException("invalid massage", msg.decoderResult().cause());
        }
        if (msg.fixedHeader() == null) {
            throw new IOException("Unknown packet, no fixedHeader present, no cause provided");
        }
        return msg;
    }

    private MqttNettyUtils() {
    }
}
