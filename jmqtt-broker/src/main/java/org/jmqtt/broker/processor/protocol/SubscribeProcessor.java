package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.acl.AuthValid;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.common.model.Topic;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 订阅报文逻辑处理
 * TODO mqtt5协议支持
 */
public class SubscribeProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.messageTraceLog;

    private SubscriptionMatcher subscriptionMatcher;
    private AuthValid           authValid;
    private MessageStore        messageStore;
    private SessionStore        sessionStore;

    public SubscribeProcessor(BrokerController controller){
        this.subscriptionMatcher = controller.getSubscriptionMatcher();
        this.authValid = controller.getAuthValid();
        this.sessionStore = controller.getSessionStore();
        this.messageStore = controller.getMessageStore();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = subscribeMessage.variableHeader().messageId();
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        List<Topic> validTopicList =validTopics(clientSession,subscribeMessage.payload().topicSubscriptions());
        if(validTopicList == null || validTopicList.size() == 0){
            LogUtil.warn(log,"[Subscribe] -> Valid all subscribe topic failure,clientId:{}",clientId);
            return;
        }
        List<Integer> ackQos = getTopicQos(validTopicList);
        MqttMessage subAckMessage = MessageUtil.getSubAckMessage(messageId,ackQos);
        ctx.writeAndFlush(subAckMessage);
        // send retain messages
        List<Message> retainMessages = subscribe(clientSession,validTopicList);
        dispatcherRetainMessage(clientSession,retainMessages);
    }

    private List<Integer> getTopicQos(List<Topic> topics){
        List<Integer> qoss = new ArrayList<>(topics.size());
        for(Topic topic : topics){
            qoss.add(topic.getQos());
        }
        return qoss;
    }

    private List<Message> subscribe(ClientSession clientSession,List<Topic> validTopicList){
        Collection<Message> retainMessages = null;
        List<Message> needDispatcher = new ArrayList<>();
        for(Topic topic : validTopicList){
            Subscription subscription = new Subscription(clientSession.getClientId(),topic.getTopicName(),topic.getQos());
            boolean subRs = this.subscriptionMatcher.subscribe(subscription);
            if(subRs){
                if(retainMessages == null){
                    retainMessages = messageStore.getAllRetainMsg(); // TODO 这里需要优化，不能一次获取所有retain消息，retain消息太多可能导致broker crash或者hang住
                }
                if (!MixAll.isEmpty(retainMessages)) {
                    for(Message retainMsg : retainMessages){
                        String pubTopic = (String) retainMsg.getHeader(MessageHeader.TOPIC);
                        if(subscriptionMatcher.isMatch(pubTopic,subscription.getTopic())){
                            int minQos = MessageUtil.getMinQos((int)retainMsg.getHeader(MessageHeader.QOS),topic.getQos());
                            retainMsg.putHeader(MessageHeader.QOS,minQos);
                            needDispatcher.add(retainMsg);
                        }
                    }
                }
            }
            this.sessionStore.storeSubscription(clientSession.getClientId(),subscription);
        }
        return needDispatcher;
    }

    /**
     * 返回校验合法的topic
     */
    private List<Topic> validTopics(ClientSession clientSession,List<MqttTopicSubscription> topics){
        List<Topic> topicList = new ArrayList<>();
        for(MqttTopicSubscription subscription : topics){
            if(!authValid.subscribeVerify(clientSession.getClientId(),subscription.topicName())){
                LogUtil.warn(log,"[SubPermission] this clientId:{} have no permission to subscribe this topic:{}",clientSession.getClientId(),subscription.topicName());
                clientSession.getCtx().close();
                return null;
            }
            Topic topic = new Topic(subscription.topicName(),subscription.qualityOfService().value());
            topicList.add(topic);
        }
        return topicList;
    }

    /**
     * 分发retain消息:
     * TODO 待优化，retain消息逻辑需要优化：1.性能优化；2.逻辑放到MessageDispatcher统一处理
     */
    private void dispatcherRetainMessage(ClientSession clientSession,List<Message> messages){
        for(Message message : messages){
            message.putHeader(MessageHeader.RETAIN,true);
            int qos = (int) message.getHeader(MessageHeader.QOS);
            if(qos > 0){
                sessionStore.cacheInflowMsg(clientSession.getClientId(),message);
            }
            message.setMsgId(clientSession.generateMessageId());
            MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false);
            clientSession.getCtx().writeAndFlush(publishMessage);
        }
    }

}
