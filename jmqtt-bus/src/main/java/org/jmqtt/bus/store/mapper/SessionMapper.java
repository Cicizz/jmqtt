
package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jmqtt.bus.store.daoobject.SessionDO;

public interface SessionMapper {

    @Select("select client_id,online,transport_protocol,client_ip,server_ip,last_offline_time,online_time,properties from jmqtt_session where client_id = #{clientId}")
    SessionDO getSession(String clientId);

    @Insert("insert into jmqtt_session(client_id,online,transport_protocol,client_ip,server_ip,last_offline_time,online_time,properties) values "
            + "(#{clientId},#{online},#{transportProtocol},#{clientIp},#{serverIp},#{lastOfflineTime},#{onlineTime},#{properties}) "
            + "on DUPLICATE key update online = #{online},last_offline_time = #{lastOfflineTime},online_time = #{onlineTime}"
            + ",client_ip=#{clientIp},server_ip=#{serverIp},transport_protocol=#{transportProtocol}")
    @Options(useGeneratedKeys=true,keyProperty="id")
    Long storeSession(SessionDO sessionDO);

    @Update("update jmqtt_session set last_offline_time = #{lastOfflineTime},online = #{online} where "
            + "client_id = #{clientId}")
    Long offline(SessionDO sessionDO);

}
