package org.jmqtt.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * inner message transfer from MqttMessage
 */
public class Message {

    private int msgId;

    private Map<String,Object> headers;

    private ClientSession clientSession;

    private Type type;

    private Object payload;

    public Message(){};

    public Message(Type type,Map<String,Object> headers,Object payload){
        this.type = type;
        this.headers = headers;
        this.payload = payload;
    }

    public Object putHeader(String key,Object value){
        if(headers == null){
            headers = new HashMap<>();
        }
        return headers.put(key,value);
    };

    public Object removeHeader(String key){
        return headers.remove(key);
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    /**
     * mqtt message type
     */
    public enum Type{
        CONNECT(1),
        CONNACK(2),
        PUBLISH(3),
        PUBACK(4),
        PUBREC(5),
        PUBREL(6),
        PUBCOMP(7),
        SUBSCRIBE(8),
        SUBACK(9),
        UNSUBSCRIBE(10),
        UNSUBACK(11),
        PINGREQ(12),
        PINGRESP(13),
        DISCONNECT(14),
        WILL(15);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static Type valueOf(int type) {
            Type[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Type t = var1[var3];
                if (t.value == type) {
                    return t;
                }
            }
            throw new IllegalArgumentException("unknown message type: " + type);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", headers=" + headers +
                ", clientSession=" + clientSession +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}
