package org.jmqtt.bus.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.DeviceSessionManager;
import org.jmqtt.bus.enums.DeviceOnlineStateEnum;
import org.jmqtt.bus.enums.TransportProtocolEnum;
import org.jmqtt.bus.model.DeviceSession;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.SessionDO;

import java.util.Date;
import java.util.Map;

public class DeviceSessionManagerImpl implements DeviceSessionManager {

    @Override
    public DeviceSession getSession(String clientId) {

        // optimize: add cache
        SessionDO sessionDO = (SessionDO) DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.sessionMapperClass).getSession(clientId);
            }
        });
        if (sessionDO == null) {
            return null;
        }
        return convert(sessionDO);
    }

    @Override
    public void storeSession(DeviceSession deviceSession) {
        SessionDO sessionDO = convert(deviceSession);
        DBUtils.operate(new DBCallback() {
            @Override
            public Long operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.sessionMapperClass).storeSession(sessionDO);
            }
        });
    }

    @Override
    public void offline(String clientId) {
        SessionDO sessionDO = new SessionDO();
        sessionDO.setClientId(clientId);
        sessionDO.setOnline(DeviceOnlineStateEnum.OFFLINE.getCode());
        sessionDO.setLastOfflineTime(new Date());
        DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.sessionMapperClass).offline(sessionDO);
            }
        });
    }

    private DeviceSession convert(SessionDO sessionDO) {
        DeviceSession deviceSession = new DeviceSession();
        deviceSession.setClientId(sessionDO.getClientId());
        deviceSession.setClientIp(sessionDO.getClientIp());
        deviceSession.setLastOfflineTime(sessionDO.getLastOfflineTime());
        deviceSession.setOnline(DeviceOnlineStateEnum.valueOf(sessionDO.getOnline()));
        deviceSession.setOnlineTime(sessionDO.getOnlineTime());
        String properties = sessionDO.getProperties();
        if (properties != null) {
            deviceSession.setProperties(JSONObject.parseObject(properties, Map.class));
        }
        deviceSession.setServerIp(deviceSession.getServerIp());
        deviceSession.setTransportProtocol(TransportProtocolEnum.valueOf(sessionDO.getTransportProtocol()));
        return deviceSession;
    }

    private SessionDO convert(DeviceSession deviceSession) {
        SessionDO sessionDO = new SessionDO();
        sessionDO.setClientId(deviceSession.getClientId());
        sessionDO.setClientIp(deviceSession.getClientIp());
        sessionDO.setLastOfflineTime(deviceSession.getLastOfflineTime());
        sessionDO.setOnline(deviceSession.getOnline().getCode());
        sessionDO.setOnlineTime(deviceSession.getOnlineTime());
        sessionDO.setProperties(deviceSession.getProperties() == null ? null : JSONObject.toJSONString(deviceSession.getProperties()));
        sessionDO.setServerIp(deviceSession.getServerIp());
        sessionDO.setTransportProtocol(deviceSession.getTransportProtocol().getCode());
        return sessionDO;
    }
}
