package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.bean.Topic;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;

import java.util.ArrayList;
import java.util.List;

public class SubscribeProcessor implements RequestProcessor {

    private SubscriptionMatcher subscriptionMatcher;

    public SubscribeProcessor(SubscriptionMatcher subscriptionMatcher){
        this.subscriptionMatcher = subscriptionMatcher;
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
        //TODO 分发retain消息

        MqttMessage subAckMessage = MessageUtil.getSubAckMessage(messageId,topicQosList);
        ctx.writeAndFlush(subAckMessage);
    }

    private List<Integer> subscribe(ClientSession clientSession,List<Topic> validTopicList){
        List<Integer> topicQosList = new ArrayList<>();
        for(Topic topic : validTopicList){
            Subscription subscription = new Subscription(clientSession.getClientId(),topic.getTopicName(),topic.getQos());
            clientSession.subscribe(subscription);
            this.subscriptionMatcher.subscribe(topic.getTopicName(),clientSession.getClientId());
            topicQosList.add(topic.getQos());
        }
        return topicQosList;
    }

    //返回校验合法的topic
    private List<Topic> validTopics(List<MqttTopicSubscription> topics){
        List<Topic> topicList = new ArrayList<>();
        for(MqttTopicSubscription subscription : topics){
            // TODO 校验
            Topic topic = new Topic(subscription.topicName(),subscription.qualityOfService().value());
            topicList.add(topic);
        }
        return topicList;
    }
}
