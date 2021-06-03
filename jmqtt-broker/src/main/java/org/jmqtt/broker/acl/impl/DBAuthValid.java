package org.jmqtt.broker.acl.impl;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.acl.AuthValid;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.TenantContext;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.TenantInfo;
import org.jmqtt.broker.remoting.session.AuthManager;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.store.rdb.AbstractDBStore;
import org.jmqtt.broker.store.rdb.DBCallback;
import org.jmqtt.broker.store.rdb.daoobject.DeviceTenant;
import org.slf4j.Logger;

/**
 * 物联网平台连接校验
 */
public class DBAuthValid extends AbstractDBStore implements AuthValid {

    private static final Logger log = JmqttLogger.authorityLog;

    private static final String SPLIT = "@";

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean clientIdVerify(String clientId) {
        String[] arr = clientId.split(SPLIT);
        if (arr.length != 2) {
            LogUtil.error(log,"clientId is not supported",clientId);
            return false;
        }
        return true;
    }

    @Override
    public boolean onBlacklist(String remoteAddr, String clientId) {
        return false;
    }

    @Override
    public boolean authentication(String clientId, String userName, byte[] password) {
        TenantInfo tenantInfo = new TenantInfo();
        String[] arr = userName.split(SPLIT);
        if (arr.length != 2) {
            LogUtil.error(log,"clientId,username is not supported",clientId,userName);
            return false;
        }
        String tenantCode = arr[0];
        String deviceCode = arr[1];
        DeviceTenant deviceDO = (DeviceTenant) operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return getMapper(sqlSession,deviceMapperClass).getDevice(tenantCode,deviceCode);
            }
        });
        if (deviceDO == null) {
            LogUtil.error(log,"device has not register",clientId,userName);
            return false;
        }
        String passwordStr = new String(password);
        if (deviceDO.getDeviceKey().equals(passwordStr)) {
            tenantInfo.setBizCode(deviceDO.getBizCode());
            tenantInfo.setTenantCode(deviceDO.getTenantCode());
            TenantContext.setAuthInfo(tenantInfo);
            return true;
        }
        LogUtil.error(log,"client password is wrong",clientId,userName, deviceDO);
        return false;
    }

    @Override
    public boolean verifyHeartbeatTime(String clientId, int time) {
        return true;
    }

    @Override
    public boolean publishVerify(String clientId, String topic) {
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        if (clientSession == null) {
            LogUtil.warn(log,"ClientSession is not exist,clientId:{}",clientId);
            return false;
        }
        if (AuthManager.getInstance().hasPub(clientSession.getTenantCode(),topic)) {
            return true;
        }
        LogUtil.warn(log,"Pub auth failed,clientId:{},topic:{}",clientId,topic);
        return false;
    }

    @Override
    public boolean subscribeVerify(String clientId, String topic) {
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        if (clientSession == null) {
            LogUtil.warn(log,"ClientSession is not exist,clientId:{}",clientId);
            return false;
        }
        if (AuthManager.getInstance().hasSub(clientSession.getTenantCode(),topic)) {
            return true;
        }
        LogUtil.warn(log,"Sub auth failed,clientId:{},topic:{}",clientId,topic);
        return false;
    }


}
