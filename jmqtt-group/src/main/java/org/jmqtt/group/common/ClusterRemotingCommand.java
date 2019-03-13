package org.jmqtt.group.common;

import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * cluster remoting command
 */
public class ClusterRemotingCommand {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private AtomicInteger requestId = new AtomicInteger(0);

    /**
     * cluster request code
     */
    private int code;
    private int flag;
    private int opaque = requestId.incrementAndGet();
    private HashMap<String,String> extField;
    private transient byte[] body;

    public ClusterRemotingCommand(int code) {
        this.code = code;
    }

    public AtomicInteger getRequestId() {
        return requestId;
    }

    public void setRequestId(AtomicInteger requestId) {
        this.requestId = requestId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public HashMap<String, String> getExtField() {
        return extField;
    }

    public void setExtField(HashMap<String, String> extField) {
        this.extField = extField;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ClusterRemotingCommand{" +
                "requestId=" + requestId +
                ", code=" + code +
                ", flag=" + flag +
                ", opaque=" + opaque +
                ", extField=" + extField +
                '}';
    }
}
