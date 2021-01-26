
package org.jmqtt.broker.store;

/**
 * 会话状态
 */
public class SessionState {


    private StateEnum state;

    private long offlineTime;

    public SessionState(StateEnum state) {
        this.state = state;
    }

    public SessionState(StateEnum state, long offlineTime) {
        this.state = state;
        this.offlineTime = offlineTime;
    }

    public StateEnum getState() {
        return state;
    }

    public long getOfflineTime() {
        return offlineTime;
    }

    public enum StateEnum {
        /**
         * 从未连接过（之前 cleanStart为1 的也为为NULL）
         */
        NULL("NULL"),
        /**
         * 在线
         */
        ONLINE("ONLINE"),
        /**
         * cleanStart为0，且连接过Jmqtt集群，已离线，会返回offlineTime（离线时间）
         */
        OFFLINE("OFFLINE"),
        ;

        private String code;

        StateEnum(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
