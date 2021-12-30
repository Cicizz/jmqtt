package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.bus.store.daoobject.SubscriptionDO;

import java.util.List;

public interface SubscriptionMapper {

    @Insert("INSERT INTO jmqtt_subscription (client_id,topic,properties) "
            + "VALUES (#{clientId},#{topic},#{properties})")
    Long storeSubscription(SubscriptionDO subscriptionDO);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId}")
    Integer clearSubscription(String clientId);

    @Delete("DELETE FROM jmqtt_subscription WHERE client_id = #{clientId} AND topic = #{topic}")
    Integer delSubscription(@Param("clientId") String clientId, @Param("topic") String topic);

    @Select("SELECT client_id,topic,properties FROM jmqtt_subscription WHERE client_id = #{clientId}")
    List<SubscriptionDO> querySubscription(String clientId);
}
