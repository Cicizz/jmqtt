package org.jmqtt.bus.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.DeviceSubscriptionManager;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.SubscriptionDO;
import org.jmqtt.bus.subscription.SubscriptionMatcher;
import org.jmqtt.bus.subscription.Topic;
import org.jmqtt.bus.subscription.model.Subscription;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DeviceSubscriptionManagerImpl implements DeviceSubscriptionManager {

    private static final Logger log = JmqttLogger.busLog;

    private SubscriptionMatcher subscriptionMatcher;

    public DeviceSubscriptionManagerImpl(SubscriptionMatcher subscriptionMatcher){
        this.subscriptionMatcher = subscriptionMatcher;
    }

    @Override
    public boolean subscribe(DeviceSubscription deviceSubscription) {
        SqlSession sqlSession = null;
        try {
            sqlSession = DBUtils.getSqlSessionWithTrans();
            SubscriptionDO subscriptionDO = convert(deviceSubscription);
            DBUtils.getMapper(sqlSession,DBUtils.subscriptionMapperClass).storeSubscription(subscriptionDO);
            if (subscriptionDO.getId() == null || subscriptionDO.getId() <= 0) {
                LogUtil.error(log,"[SUBSCRIBE] store subscription fail.");
                return false;
            }
            Subscription subscription = convert2Sub(deviceSubscription);
            boolean addSubTree = subscriptionMatcher.subscribe(subscription);
            if (!addSubTree) {
                LogUtil.error(log,"[SUBSCRIBE] sub tree fail.");
                sqlSession.rollback();
                return false;
            }
            sqlSession.commit();
            return true;
        } catch (Exception ex) {
            LogUtil.error(log,"[SUBSCRIBE] subscribe failure.ex:{}",ex);
            if (sqlSession != null) {
                sqlSession.rollback();
            }
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        return false;
    }

    @Override
    public boolean unSubscribe(String clientId,String topic) {
        SqlSession sqlSession = null;
        try {
            sqlSession = DBUtils.getSqlSessionWithTrans();
            Integer effectNum = DBUtils.getMapper(sqlSession,DBUtils.subscriptionMapperClass).delSubscription(clientId,topic);
            if (effectNum == null || effectNum <= 0) {
                LogUtil.error(log,"[UNSUBSCRIBE] del subscription fail.");
                return false;
            }
            boolean subSub = subscriptionMatcher.unSubscribe(topic,clientId);
            if (!subSub) {
                LogUtil.error(log,"[SUBSUBSCRIBE] unsub from tree fail.");
                sqlSession.rollback();
                return false;
            }
            sqlSession.commit();
            return true;
        } catch (Exception ex) {
            LogUtil.error(log,"[SUBSCRIBE] subscribe failure.ex:{}",ex);
            if (sqlSession != null) {
                sqlSession.rollback();
            }
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        return false;
    }

    @Override
    public boolean isMatch(String pubTopic, String subTopic) {
        Topic topic = new Topic(pubTopic);
        return topic.match(new Topic(subTopic));
    }

    @Override
    public boolean onlySubscribe2Tree(DeviceSubscription deviceSubscription) {
        Subscription subscription = convert2Sub(deviceSubscription);
        return subscriptionMatcher.subscribe(subscription);
    }

    @Override
    public boolean onlyUnUnSubscribeFromTree(String clientId, String topic) {
        return subscriptionMatcher.unSubscribe(topic,clientId);
    }

    @Override
    public Set<DeviceSubscription> getAllSubscription(String clientId) {
        List<SubscriptionDO> subscriptionDOS = DBUtils.operate(new DBCallback() {
            @Override
            public List<SubscriptionDO> operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.subscriptionMapperClass).getAllSubscription(clientId);
            }
        });
        if (subscriptionDOS == null) {
            return null;
        }
        Set<DeviceSubscription> subscriptions = new HashSet<>(subscriptionDOS.size());
        subscriptionDOS.forEach(item -> {
            subscriptions.add(convert(item));
        });
        return subscriptions;
    }

    @Override
    public void deleteAllSubscription(String clientId) {
        DBUtils.operate(new DBCallback() {
            @Override
            public Integer operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.subscriptionMapperClass).clearSubscription(clientId);
            }
        });
    }

    private Subscription convert2Sub(DeviceSubscription deviceSubscription){
        return new Subscription(deviceSubscription.getClientId(),deviceSubscription.getTopic(),deviceSubscription.getProperties());
    }

    private DeviceSubscription convert(SubscriptionDO subscriptionDO){
        DeviceSubscription deviceSubscription = new DeviceSubscription();
        deviceSubscription.setClientId(subscriptionDO.getClientId());
        deviceSubscription.setTopic(subscriptionDO.getTopic());
        deviceSubscription.setSubscribeTime(subscriptionDO.getSubscribeTime());
        if (subscriptionDO.getProperties() != null) {
            deviceSubscription.setProperties(JSONObject.parseObject(subscriptionDO.getProperties(), Map.class));
        }
        return deviceSubscription;
    }

    private SubscriptionDO convert(DeviceSubscription deviceSubscription){
        SubscriptionDO subscriptionDO = new SubscriptionDO();
        subscriptionDO.setClientId(deviceSubscription.getClientId());
        subscriptionDO.setTopic(deviceSubscription.getTopic());
        subscriptionDO.setProperties(deviceSubscription.getProperties() != null? JSONObject.toJSONString(deviceSubscription.getProperties()): null);
        subscriptionDO.setSubscribeTime(deviceSubscription.getSubscribeTime());
        return subscriptionDO;
    }
}
