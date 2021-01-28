
package org.jmqtt.broker.store.rdb;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.ClusterEvent;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.rdb.daoobject.EventDO;
import org.jmqtt.broker.store.rdb.daoobject.SessionDO;
import org.jmqtt.broker.store.rdb.daoobject.SubscriptionDO;
import org.jmqtt.broker.store.rdb.mapper.EventMapper;

import java.util.*;

public class RDBSessionStore extends AbstractDBStore implements SessionStore {


    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public SessionState getSession(String clientId) {
        SessionDO sessionDO = getMapper(sessionMapperClass).getSession(clientId);
        if (sessionDO == null) {
            return new SessionState(SessionState.StateEnum.NULL);
        }
        return new SessionState(SessionState.StateEnum.valueOf(sessionDO.getState()),sessionDO.getOfflineTime());
    }

    @Override
    public boolean storeSession(String clientId, SessionState sessionState, boolean notifyClearOtherSession) {
        SessionDO sessionDO = new SessionDO();
        sessionDO.setClientId(clientId);
        sessionDO.setState(sessionState.getState().getCode());
        sessionDO.setOfflineTime(sessionState.getOfflineTime());

        if (!notifyClearOtherSession) {
            getMapper(sessionMapperClass).storeSession(sessionDO);
        } else {
            SqlSession session = getSessionWithTrans();
            try {
                // 1. 存储session
                session.getMapper(sessionMapperClass).storeSession(sessionDO);
                // 2. 存储事件
                EventDO eventDO = new EventDO();
                eventDO.setContent(clientId);
                eventDO.setEventCode(ClusterEvent.CLEAR_SESSION.getCode());
                eventDO.setGmtCreate(new Date());
                eventDO.setJmqttIp(MixAll.getLocalIp());
                session.getMapper(EventMapper.class).sendEvent(eventDO);
                session.commit();
            } catch (Exception ex) {
                log.error("StoreSession with trans error,{},{},{}",clientId,sessionState,ex);
                session.rollback(true);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        SubscriptionDO subscriptionDO = new SubscriptionDO();
        subscriptionDO.setClientId(clientId);
        subscriptionDO.setTopic(subscription.getTopic());
        subscriptionDO.setQos(subscription.getQos());
        Long id = getMapper(subscriptionMapperClass).storeSubscription(subscriptionDO);
        if (id != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        Integer effectNum = getMapper(subscriptionMapperClass).clearSubscription(clientId);
        log.debug("[ClearSubscription] effect num:{}",effectNum);
        return true;
    }

    @Override
    public boolean delSubscription(String clientId, String topic) {
        Integer effectNum = getMapper(subscriptionMapperClass).delSubscription(clientId,topic);
        if (effectNum != null && effectNum > 0) {
            return true;
        }
        log.warn("[DelSubscription]  subscription is not exist:{},{}",clientId,topic);
        return false;
    }

    @Override
    public Set<Subscription> getSubscriptions(String clientId) {
        List<SubscriptionDO> subscriptionDOList = getMapper(subscriptionMapperClass).querySubscription(clientId);
        Set<Subscription> set = new HashSet<>();
        for (SubscriptionDO item : subscriptionDOList) {
            Subscription subscription = new Subscription(item.getClientId(),item.getTopic(),item.getQos());
            set.add(subscription);
        }
        return set;
    }

    @Override
    public boolean cacheInflowMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        return null;
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public boolean containOutflowMsg(String clientId, int msgId) {
        return false;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        return null;
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        return null;
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        return false;
    }
}
