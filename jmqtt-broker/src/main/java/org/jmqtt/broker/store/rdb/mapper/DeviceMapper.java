package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jmqtt.broker.store.rdb.daoobject.DeviceDO;

public interface DeviceMapper {

    @Select("SELECT id,device_coding,device_key,product_id,biz_code,tenant_code FROM jmq_device WHERE device_status = 0 and tenant_code = #{tenantCode} and device_coding = #{deviceCoding}")
    DeviceDO getDevice(@Param("tenantCode") String tenantCode, @Param("deviceCoding") String deviceCoding);

    @Update("UPDATE jmq_device set latest_online_time = #{latestOnlineTime},is_online = #{isOnline},latest_network_address = #{latestNetworkAddress}"
            + " where tenant_code = #{tenantCode} and device_coding = #{deviceCoding}")
    int updateDevice(DeviceDO deviceDO);
}
