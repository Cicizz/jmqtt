
package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.SessionDO;

public interface SessionMapper {

    @Select("select client_id,state,offline_time from jmqtt_session where client_id = #{clientId}")
    SessionDO getSession(String clientId);

    @Insert("insert into jmqtt_session(client_id,state,offline_time,biz_code,tenant_code) values "
            + "(#{clientId},#{state},#{offlineTime},#{bizCode},#{tenantCode}) "
            + "on DUPLICATE key update state = #{state},offline_time = #{offlineTime}")
    Long storeSession(SessionDO sessionDO);

}
