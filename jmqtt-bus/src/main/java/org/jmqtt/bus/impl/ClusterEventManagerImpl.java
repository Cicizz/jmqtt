package org.jmqtt.bus.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.ClusterEventManager;
import org.jmqtt.bus.enums.ClusterEventCodeEnum;
import org.jmqtt.bus.event.EventCenter;
import org.jmqtt.bus.event.GatewayListener;
import org.jmqtt.bus.model.ClusterEvent;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.EventDO;
import org.jmqtt.bus.subscription.SubscriptionMatcher;
import org.jmqtt.bus.subscription.model.Subscription;
import org.jmqtt.support.helper.MixAll;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * send current node's event to other nodes 集群事件处理，两种方式,根据实际情况，选一个实现即可：
 * 1. 发布订阅： a. 发送消息 A -> mq系统转发 b. 第二阶段：消息订阅分发给本节点的设备 jmqtt B,C,D —> jmqtt B的连接设备
 * 2. jmqtt服务主动拉取: a. A —> 消息存储服务： b. 第二阶段:主动拉取 jmqtt B,C,D broker 定时批量拉取 —> 从消息存储服务拉取 —> 拉取后推送给 B,C,D上连接的设备
 */
public class ClusterEventManagerImpl implements ClusterEventManager {

    private static final Logger        log        = JmqttLogger.busLog;
    private static final AtomicLong    offset     = new AtomicLong();
    private          AtomicBoolean pollStoped = new AtomicBoolean(false);
    private int                    maxPollNum = 100;
    private int                    pollWaitInterval = 10; // ms
    private final EventCenter eventCenter;
    private SubscriptionMatcher subscriptionMatcher;

    public ClusterEventManagerImpl(SubscriptionMatcher subscriptionMatcher){
        this.eventCenter = new EventCenter();
        this.subscriptionMatcher = subscriptionMatcher;
    }

    @Override
    public void start() {
        Long maxId = DBUtils.operate(new DBCallback() {
            @Override
            public Long operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.eventMapperClass).getMaxOffset();
            }
        });
        if (maxId == null) {
            offset.set(0);
        } else {
            offset.set(maxId);
        }
        new Thread(() -> {
            while (!pollStoped.get()) {
                try {
                    List<ClusterEvent> eventList = pollEvent(maxPollNum);
                    if (!MixAll.isEmpty(eventList)) {
                        for (ClusterEvent event : eventList) {
                            // Send event to iot gateway
                            consumeEvent(event);
                        }
                    }
                    if (MixAll.isEmpty(eventList) || eventList.size() < 5) {
                        Thread.sleep(pollWaitInterval);
                    }
                } catch (Exception e) {
                    LogUtil.warn(log, "Poll event from cluster error.", e);
                }
            }
        }).start();

        LogUtil.info(log,"Cluster event server start.");
    }

    @Override
    public void shutdown() {
        this.pollStoped.compareAndSet(false,true);
    }

    /**
     * 集群消息往这里投递
     */
    private void consumeEvent(ClusterEvent event){

        // 若是消息，进行订阅树的匹配
        if (event.getClusterEventCode() == ClusterEventCodeEnum.DISPATCHER_CLIENT_MESSAGE){
            DeviceMessage deviceMessage = JSONObject.parseObject(event.getContent(),DeviceMessage.class);
            if (deviceMessage == null) {
                LogUtil.error(log,"[BUS EVENT] event content is empty.");
                return;
            }
            Set<Subscription> subscriptionSet = this.subscriptionMatcher.match(deviceMessage.getTopic());
            deviceMessage.setContent(null);
            if (subscriptionSet != null) {
                subscriptionSet.forEach(item -> {
                    ClusterEvent stayEvent = event.clone();
                    stayEvent.setSubscription(item);

                    eventCenter.sendEvent(stayEvent);
                });
            }
            return;
        }
        eventCenter.sendEvent(event);
    }


    private List<ClusterEvent> pollEvent(int maxPollNum) {
        // offset: min -> max
        long currentOffset = offset.get();
        List<EventDO> eventDOList =  DBUtils.operate(new DBCallback() {
            @Override
            public List<EventDO> operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.eventMapperClass).consumeEvent(currentOffset,maxPollNum);
            }
        });
        if (eventDOList == null || eventDOList.size() == 0) {
            return Collections.emptyList();
        }
        List<ClusterEvent> events = new ArrayList<>();
        for (EventDO eventDO : eventDOList) {
            ClusterEvent event = new ClusterEvent();
            event.setNodeIp(eventDO.getNodeIp());
            event.setGmtCreate(eventDO.getGmtCreate());
            event.setContent(eventDO.getContent());
            event.setClusterEventCode(ClusterEventCodeEnum.valueOf(eventDO.getEventCode()));
            events.add(event);
        }

        // reset offset
        EventDO eventDO = eventDOList.get(eventDOList.size()-1);
        if (!offset.compareAndSet(currentOffset,eventDO.getId())) {
            LogUtil.warn(log,"[RDBClusterEventHandler] pollEvent offset is wrong,expectOffset:{},currentOffset:{},maxOffset:{}",
                    offset.get(),currentOffset,eventDO.getId());
            offset.set(eventDO.getId());
        }
        return events;
    }

    @Override
    public void sendEvent(ClusterEvent clusterEvent) {
        EventDO eventDO = convert(clusterEvent);
        DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.eventMapperClass).sendEvent(eventDO);
            }
        });
    }


    public void registerEventListener(GatewayListener listener){
        this.eventCenter.register(listener);
    }


    private EventDO convert(ClusterEvent clusterEvent) {
        EventDO eventDO = new EventDO();
        eventDO.setContent(clusterEvent.getContent());
        eventDO.setNodeIp(clusterEvent.getNodeIp());
        eventDO.setEventCode(clusterEvent.getClusterEventCode().getCode());
        eventDO.setGmtCreate(clusterEvent.getGmtCreate());
        return eventDO;
    }
}
