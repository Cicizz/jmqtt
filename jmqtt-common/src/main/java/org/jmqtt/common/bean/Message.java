package org.jmqtt.common.bean;

/**
 * inner message transfer from MqttMessage
 */
public class Message {


    private int msgId;

    private boolean retain;

    private Type type;

    private Object payload;

    private boolean dup;

    private int qos;


    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
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

    public boolean isDup() {
        return dup;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
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
        DISCONNECT(14);

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
}
