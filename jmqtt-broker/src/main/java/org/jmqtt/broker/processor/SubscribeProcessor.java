package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.*;
import org.jmqtt.remoting.netty.MessageDispatcher;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.RetainMessageStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubscribeProcessor implements RequestProcessor {

    private SubscriptionMatcher subscriptionMatcher;
    private RetainMessageStore retainMessageStore;
    private FlowMessageStore flowMessageStore;

    public SubscribeProcessor(SubscriptionMatcher subscriptionMatcher,RetainMessageStore retainMessageStore,FlowMessageStore flowMessageStore){
        this.subscriptionMatcher = subscriptionMatcher;
        this.retainMessageStore = retainMessageStore;
        this.flowMessageStore = flowMessageStore;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = subscribeMessage.variableHeader().messageId();
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        List<MqttTopicSubscription> recTopicList = subscribeMessage.payload().topicSubscriptions();
        List<Topic> validTopicList =validTopics(recTopicList);
        List<Integer> topicQosList = subscribe(clientSession,validTopicList);
        MqttMessage subAckMessage = MessageUtil.getSubAckMessage(messageId,topicQosList);
        ctx.writeAndFlush(subAckMessage);
    }

    private List<Integer> subscribe(ClientSession clientSession,List<Topic> validTopicList){
        List<Integer> topicQosList = new ArrayList<>();
        Collection<Message> retainMessages = new ArrayList<>();
        for(Topic topic : validTopicList){
            Subscription subscription = new Subscription(clientSession.getClientId(),topic.getTopicName(),topic.getQos());
            int subRs = this.subscriptionMatcher.subscribe(topic.getTopicName(),subscription);
            if(subRs == SubResult.SUB_FAILED){
                continue;
            }else if(subRs == SubResult.NEW_SUB){//dispatcher retain message
                if(retainMessages.isEmpty()){
                    retainMessages = retainMessageStore.getAllRetainMessage();
                }
                for(Message retainMsg : retainMessages){
                    String pubToken = (String) retainMsg.getHeader(MessageHeader.TOPIC);
                    if(subscriptionMatcher.isMatch(pubToken,subscription.getTopic())){
                        dispatcherRetainMessage(retainMsg,subscription.getQos(),clientSession);
                    }
                }
            }
            clientSession.subscribe(subscription);
            topicQosList.add(topic.getQos());
        }
        return topicQosList;
    }

    /**
     * 返回校验合法的topic
     */
    private List<Topic> validTopics(List<MqttTopicSubscription> topics){
        List<Topic> topicList = new ArrayList<>();
        for(MqttTopicSubscription subscription : topics){
            // TODO 校验
            Topic topic = new Topic(subscription.topicName(),subscription.qualityOfService().value());
            topicList.add(topic);
        }
        return topicList;
    }

    /**
     * 分发retain消息给新订阅者
     */
    private void dispatcherRetainMessage(Message message,int subQos,ClientSession clientSession){
        message.putHeader(MessageHeader.RETAIN,true);
        int qos = (int) message.getHeader(MessageHeader.QOS);
        int minQos = MessageUtil.getMinQos(qos,subQos);
        if(minQos > 0){
            flowMessageStore.cacheSendMsg(clientSession.getClientId(),message);
        }
        MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false,minQos,clientSession.generateMessageId());
        clientSession.getCtx().writeAndFlush(publishMessage);
    }

    private class SubResult{
        public static final int SUB_FAILED = 0;
        public static final int NEW_SUB = 1;
        public static final int REPEAT_SUB = 2;
    }
}
