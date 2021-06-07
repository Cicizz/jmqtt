package org.jmqtt.broker.monitor;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.TenantInfo;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.store.redis.RedisCallBack;
import org.jmqtt.broker.store.redis.support.RedisReplyUtils;
import org.jmqtt.broker.store.redis.support.RedisSupport;
import org.jmqtt.broker.store.redis.support.RedisUtils;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

public class MonitorHandler {

    private static final Logger log = JmqttLogger.monitorLog;

    private static final String PREFIX = "JMQ";

    // 发送消息总数-jmq_admin进行统计，按分钟统计，也是分钟级消息tps:租户和业务维度
    private static final String PUB_MSG_CNT_TREND = PREFIX + "_PUB_MSG_CNT_TREND_";

    // 活跃数：记录clientId在线的个数：近一天有发送，订阅，消费消息的设备数：
    private static final String ACTIVE_CNT_TREND = PREFIX + "_ACTIVE_CNT_TREND_";

    // 连接请求数：每1小时统计，按租户业务维度
    private static final String CONN_REQ_CNT_TREND = PREFIX + "_CONN_REQ_CNT_TREND_";

    // 连接请求数：每1小时统计，按租户业务维度
    private static final String CONN_REQ_CNT_SUCC_TREND = PREFIX + "_CONN_REQ_CNT_SUCC_TREND_";

    private RedisSupport redisSupport;

    private BrokerConfig brokerConfig;

    public MonitorHandler(BrokerConfig brokerConfig){
        this.brokerConfig = brokerConfig;
    }

    // 记录发送消息
    public void recordPubMsg(String clientId){

        if (!RedisReplyUtils.isOk(redisSupport.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.incr(PUB_MSG_CNT_TREND+tenantAndBiz(clientId));
            }
        }))){
            LogUtil.error(log,"Monitor recordPubMsg error,clientId:{}",clientId);
        }
    }

    // 记录连接请求次数
    public void recordConnReq(TenantInfo tenantInfo){

        if (!RedisReplyUtils.isOk(redisSupport.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.incr(CONN_REQ_CNT_TREND+tenantInfo.getTenantCode() + "_" + tenantInfo.getBizCode());
            }
        }))){
            LogUtil.error(log,"Monitor recordConnReq error,clientId:{}",tenantInfo);
        }
    }

    // 记录连接请求成功次数
    public void recordConnSuccReq(String clientId){

        if (!RedisReplyUtils.isOk(redisSupport.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.incr(CONN_REQ_CNT_SUCC_TREND+tenantAndBiz(clientId));
            }
        }))){
            LogUtil.error(log,"Monitor recordConnSuccReq error,clientId:{}",clientId);
        }
    }

    // 记录设备活跃状态：发送消息，订阅，消费消息都算活跃
    public void recordActiveClient(String clientId){

        if (!RedisReplyUtils.isOk(redisSupport.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hincrBy(CONN_REQ_CNT_SUCC_TREND+tenantAndBiz(clientId),clientId,1);
            }
        }))){
            LogUtil.error(log,"Monitor recordActiveClient error,clientId:{}",clientId);
        }
    }

    private String tenantAndBiz(String clientId){
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        return clientSession.getTenantCode() + "_" + clientSession.getBizCode();
    }

    public void start(){
        this.redisSupport = RedisUtils.getInstance().createSupport(brokerConfig);
    }

    public void shutdown(){
        RedisUtils.getInstance().close();
    }
}
