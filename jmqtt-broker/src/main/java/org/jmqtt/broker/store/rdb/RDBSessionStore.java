
package org.jmqtt.broker.store.rdb;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.ClusterEvent;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.rdb.daoobject.*;
import org.jmqtt.broker.store.rdb.mapper.InflowMessageMapper;
import org.jmqtt.broker.store.rdb.mapper.OutflowMessageMapper;
import org.jmqtt.broker.store.rdb.mapper.OutflowSecMessageMapper;

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
                eventDO.setGmtCreate(System.currentTimeMillis());
                eventDO.setJmqttIp(MixAll.getLocalIp());
                session.getMapper(eventMapperClass).sendEvent(eventDO);
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
        return id != null;
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
        InflowMessageDO inflowMessageDO = new InflowMessageDO();
        inflowMessageDO.setClientId(clientId);
        inflowMessageDO.setMsgId(message.getMsgId());
        inflowMessageDO.setContent(JSONObject.toJSONString(message));
        inflowMessageDO.setGmtCreate(message.getStoreTime());
        Long id = getMapper(inflowMessageMapperClass).cacheInflowMessage(inflowMessageDO);
        return id != null;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        SqlSession sqlSession = getSessionWithTrans();
        InflowMessageDO inflowMessageDO = null;
        try {
            InflowMessageMapper inflowMessageMapper = sqlSession.getMapper(inflowMessageMapperClass);
            inflowMessageDO = inflowMessageMapper.getInflowMessage(clientId,msgId);
            if (inflowMessageDO == null) {
                return null;
            }
            inflowMessageMapper.delInflowMessage(inflowMessageDO.getId());
        } catch (Exception ex) {
            log.error("DB cacheInflowMsg error,{}",ex);
            sqlSession.rollback();
        }
        return JSONObject.parseObject(inflowMessageDO.getContent(),Message.class);
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        List<InflowMessageDO> messageList = getMapper(inflowMessageMapperClass).getAllInflowMessage(clientId);
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (InflowMessageDO inflowMessageDO : messageList) {
            Message message = JSONObject.parseObject(inflowMessageDO.getContent(),Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        OutflowMessageDO outflowMessageDO = new OutflowMessageDO();
        outflowMessageDO.setClientId(clientId);
        outflowMessageDO.setMsgId(message.getMsgId());
        outflowMessageDO.setContent(JSONObject.toJSONString(message));
        outflowMessageDO.setGmtCreate(message.getStoreTime());
        Long id = getMapper(outflowMessageMapperClass).cacheOuflowMessage(outflowMessageDO);
        return id != null;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        List<OutflowMessageDO> messageList = getMapper(outflowMessageMapperClass).getAllOutflowMessage(clientId);
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (OutflowMessageDO outflowMessageDO : messageList) {
            Message message = JSONObject.parseObject(outflowMessageDO.getContent(),Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        SqlSession sqlSession = getSessionWithTrans();
        OutflowMessageDO outflowMessageDO = null;
        try {
            OutflowMessageMapper outflowMessageMapper = sqlSession.getMapper(outflowMessageMapperClass);
            outflowMessageDO = outflowMessageMapper.getOutflowMessage(clientId,msgId);
            if (outflowMessageDO == null) {
                return null;
            }
            outflowMessageMapper.delOutflowMessage(outflowMessageDO.getId());
        } catch (Exception ex) {
            log.error("DB cacheInflowMsg error,{}",ex);
            sqlSession.rollback();
        }
        return JSONObject.parseObject(outflowMessageDO.getContent(),Message.class);
    }

    @Override
    public boolean cacheOutflowSecMsgId(String clientId, int msgId) {
        OutflowSecMessageDO outflowSecMessageDO = new OutflowSecMessageDO();
        outflowSecMessageDO.setClientId(clientId);
        outflowSecMessageDO.setMsgId(msgId);
        outflowSecMessageDO.setGmtCreate(System.currentTimeMillis());
        Long id = getMapper(outflowSecMessageMapperClass).cacheOuflowMessage(outflowSecMessageDO);
        return id != null;
    }

    @Override
    public boolean releaseOutflowSecMsgId(String clientId, int msgId) {
        SqlSession sqlSession = getSessionWithTrans();
        try {
            OutflowSecMessageMapper outflowSecMessageMapper = sqlSession.getMapper(outflowSecMessageMapperClass);
            OutflowSecMessageDO outflowSecMessageDO = outflowSecMessageMapper.getOutflowSecMessage(clientId,msgId);
            if (outflowSecMessageDO == null) {
                log.error("DB releaseOutflowSecMsgId failure,msg id is not exist,{},{}",clientId,msgId);
                return false;
            }
            outflowSecMessageMapper.delOutflowSecMessage(outflowSecMessageDO.getId());
        } catch (Exception ex) {
            log.error("DB cacheInflowMsg error,{}",ex);
            sqlSession.rollback();
        }
        return true;
    }

    @Override
    public List<Integer> getAllOutflowSecMsgId(String clientId) {
        return getMapper(outflowSecMessageMapperClass).getAllOutflowSecMessage(clientId);
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        OfflineMessageDO offlineMessageDO = new OfflineMessageDO();
        offlineMessageDO.setClientId(clientId);
        offlineMessageDO.setContent(JSONObject.toJSONString(message));
        offlineMessageDO.setGmtCreate(message.getStoreTime());
        Long id = getMapper(offlineMessageMapperClass).storeOfflineMessage(offlineMessageDO);
        return id != 0;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        List<OfflineMessageDO> offlineMessageDOList = getMapper(offlineMessageMapperClass).getAllOfflineMessage(clientId);
        if (MixAll.isEmpty(offlineMessageDOList)) {
            return null;
        }
        List<Message> messageList = new ArrayList<>();
        for (OfflineMessageDO offlineMessageDO : offlineMessageDOList) {
            Message message = JSONObject.parseObject(offlineMessageDO.getContent(),Message.class);
            messageList.add(message);
        }
        return messageList;
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        Integer effectNum = getMapper(offlineMessageMapperClass).clearOfflineMessage(clientId);
        log.debug("RDB clearOfflineMsg del nums:{}",effectNum);
        return true;
    }
}
