package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.OutflowSecMessageDO;

import java.util.List;

public interface OutflowSecMessageMapper {

    @Insert("INSERT INTO jmqtt_outflow_sec_message(client_id,msg_id,gmt_create,biz_code,tenant_code) VALUES(#{clientId},#{msgId},#{gmtCreate},#{bizCode},#{tenantCode})"
            + "  on DUPLICATE key update gmt_create = #{gmtCreate}")
    Long cacheOuflowMessage(OutflowSecMessageDO outflowSecMessageDO);

    @Select("SELECT id,client_id,msg_id,gmt_create,biz_code,tenant_code FROM jmqtt_outflow_sec_message WHERE client_id = #{clientId} and msg_id = #{msgId}")
    OutflowSecMessageDO getOutflowSecMessage(@Param("clientId") String clientId, @Param("msgId") int msgId);

    @Delete("DELETE FROM jmqtt_outflow_sec_message WHERE id = #{id}")
    Integer delOutflowSecMessage(Long id);

    @Select("SELECT msg_id,biz_code,tenant_code FROM jmqtt_outflow_sec_message WHERE client_id = #{clientId} order by gmt_create asc")
    List<Integer> getAllOutflowSecMessage(String clientId);
}
