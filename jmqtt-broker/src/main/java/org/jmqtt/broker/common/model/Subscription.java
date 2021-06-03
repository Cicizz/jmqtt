package org.jmqtt.broker.common.model;

import lombok.Data;
import lombok.ToString;
import org.jmqtt.broker.store.rdb.daoobject.TenantBase;

import java.util.Objects;

/**
 * 订阅关系
 */
@Data
@ToString
public class Subscription extends TenantBase {
    private String clientId;
    private int qos;
    private String topic;

    public Subscription(String clientId,String topic,int qos){
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, topic);
    }


}
