package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.*;
import org.jmqtt.bus.store.daoobject.SubscriptionDO;

import java.util.List;

public interface SubscriptionMapper {

    @Insert("INSERT INTO jmqtt_subscription (client_id,topic,properties,subscribe_time) "
            + "VALUES (#{clientId},#{topic},#{properties},#{subscribeTime}) on duplicate key update properties = #{properties},subscribe_time=#{subscribeTime}")
    @Options(useGeneratedKeys=true,keyProperty="id")
    Long storeSubscription(SubscriptionDO subscriptionDO);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId}")
    Integer clearSubscription(String clientId);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId} AND topic = #{topic}")
    Integer delSubscription(@Param("clientId") String clientId, @Param("topic") String topic);

    @Select("SELECT id,client_id,topic,properties,subscribe_time FROM jmqtt_subscription WHERE client_id = #{clientId}")
    List<SubscriptionDO> getAllSubscription(String clientId);
}
