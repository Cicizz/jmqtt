package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.SubscriptionDO;

import java.util.List;

public interface SubscriptionMapper {

    @Insert("INSERT INTO jmqtt_subscription (client_id,topic,qos) "
            + "VALUES (#{clientId},#{topic},#{qos})")
    Long storeSubscription(SubscriptionDO subscriptionDO);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId}")
    Integer clearSubscription(String clientId);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId} AND topic = #{topic}")
    Integer delSubscription(String clientId,String topic);

    @Select("SELECT client_id,topic,qos FROM jmqtt_subscription WHERE client_id = #{clientId}")
    List<SubscriptionDO> querySubscription(String clientId);
}
