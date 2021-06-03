package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.InflowMessageTenant;

import java.util.List;

public interface InflowMessageMapper {

    @Insert("INSERT INTO jmqtt_inflow_message(client_id,msg_id,content,gmt_create,biz_code,tenant_code) VALUES(#{clientId},#{msgId},#{content},#{gmtCreate},#{bizCode},#{tenantCode})"
            + " on DUPLICATE key update content = #{content},gmt_create = #{gmtCreate}")
    Long cacheInflowMessage(InflowMessageTenant inflowMessageDO);

    @Select("SELECT id,client_id,msg_id,content,gmt_create,biz_code,tenant_code FROM jmqtt_inflow_message WHERE client_id = #{clientId} and msg_id = #{msgId}")
    InflowMessageTenant getInflowMessage(@Param("clientId") String clientId, @Param("msgId") int msgId);

    @Delete("DELETE FROM jmqtt_inflow_message WHERE id = #{id}")
    Integer delInflowMessage(Long id);

    @Select("SELECT id,client_id,msg_id,content,gmt_create,biz_code,tenant_code FROM jmqtt_inflow_message WHERE client_id = #{clientId} order by gmt_create asc")
    List<InflowMessageTenant> getAllInflowMessage(String clientId);
}
