
package org.jmqtt.broker.cluster.command;

public class CommandReqOrResp {

    private String commandCode;

    private Object body;

    public CommandReqOrResp(String commandCode) {
        this.commandCode = commandCode;
    }

    public CommandReqOrResp(String commandCode, Object body) {
        this.commandCode = commandCode;
        this.body = body;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public Object getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "CommandReqOrResp{" +
                "commandCode='" + commandCode + '\'' +
                ", body=" + body +
                '}';
    }
}