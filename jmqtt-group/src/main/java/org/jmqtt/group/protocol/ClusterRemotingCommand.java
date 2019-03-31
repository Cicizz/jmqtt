package org.jmqtt.group.protocol;

import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * cluster remoting command
 */
public class ClusterRemotingCommand {
    
    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private static final AtomicInteger requestId = new AtomicInteger(0);

    /**
     * cluster request code
     */
    private int code;
    /**
     * {@link MessageFlag}
     */
    private int flag;
    /**
     * 0:request
     * 1:response
     */
    private int rpcType = 0;
    private int opaque = requestId.incrementAndGet();
    private HashMap<String,String> extField = new HashMap<>();
    private transient byte[] body;

    public ClusterRemotingCommand(){

    }

    public ClusterRemotingCommand(int code) {
        this.code = code;
    }

    public ClusterRemotingCommand(int code,byte[] body) {
        this.code = code;
        this.body = body;
    }

    public void makeResponseType(){
        this.rpcType = 1;
    }

    public void putExtFiled(String key,String value){
        this.extField.put(key,value);
    }


    public String getExtField(String key){
        return this.extField.get(key);
    }
    public RemotingCommandType getType(){
        if(rpcType == 0){
            return RemotingCommandType.REQUEST_COMMAND;
        }
        return RemotingCommandType.RESPONSE_COMMAND;
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

    public int getRpcType() {
        return rpcType;
    }

    public void setRpcType(int rpcType)
    {
        this.rpcType = rpcType;
    }

    @Override
    public String toString() {
        return "ClusterRemotingCommand{" +
                "requestId=" + requestId +
                ", code=" + code +
                ", flag=" + flag +
                ", rpcType=" + rpcType +
                ", opaque=" + opaque +
                ", extField=" + extField +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterRemotingCommand cmd = (ClusterRemotingCommand) o;
        return code == cmd.code &&
                flag == cmd.flag &&
                rpcType == cmd.rpcType &&
                opaque == cmd.opaque &&
                Objects.equals(extField,cmd.extField) &&
                Arrays.equals(body,cmd.body);
    }
}
