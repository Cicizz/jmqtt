package org.jmqtt.broker.subscribe;

import akka.actor.ActorSelection;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.model.AkkaDefaultSerializable;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction.RoutedMessageAction;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction.RoutedMessageActionResult;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction.SubOrUnsubAction;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction.UnRoutedMessage;
import org.slf4j.Logger;

public class GroupSubscriberCollector extends AbstractBehavior<GroupSubscriptionAction> {

    private static final Logger log = JmqttLogger.messageTraceLog;


    private SubscriptionMatcher subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
    private Map<Subscription, String> subMapRemoteAddr = new HashMap<>();
    private Map<String, ActorSelection> remoteAddrMapActor = new HashMap<>();
    private String remoteAddress;
    private ActorRef<GroupMessageAction> messageHandler;


    private GroupSubscriberCollector(ActorContext<GroupSubscriptionAction> context,
        String remoteAddress, ActorRef<GroupMessageAction> messageHandler) {
        super(context);
        this.remoteAddress = remoteAddress;
        this.messageHandler = messageHandler;
    }


    public static Behavior<GroupSubscriptionAction> create(String remoteAddress,
        ActorRef<GroupMessageAction> messageHandler
    ) {
        return Behaviors.setup(
            context ->
                new GroupSubscriberCollector(context, remoteAddress, messageHandler)
        );
    }


    @Override
    public Receive<GroupSubscriptionAction> createReceive() {
        return newReceiveBuilder().onMessage(SubOrUnsubAction.class, subOrUnsubAction -> {
            if (subOrUnsubAction.isSub) {
                //订阅
                Subscription sub = subOrUnsubAction.subscription;
                String remoteAddr = subOrUnsubAction.remoteAddress;
                boolean subscribe = subscriptionMatcher.subscribe(sub);
                //记录订阅者节点地址
                if (subscribe) {
                    subMapRemoteAddr.put(sub, remoteAddr);
                }
            } else {
                //取消订阅
                Subscription sub = subOrUnsubAction.subscription;
                boolean unSubscribe = subscriptionMatcher
                    .unSubscribe(sub.getTopic(), sub.getClientId());
                if (unSubscribe) {
                    subMapRemoteAddr.remove(sub);
                }
            }
            return this;
        }).onMessage(UnRoutedMessage.class, msg -> {
            try {
                Message message = msg.message;
                Set<Subscription> subscriptions = subscriptionMatcher
                    .matchGroup((String) message.getHeader(
                        MessageHeader.TOPIC), message.getClientId());
                List<RoutedMessageAction> result = new ArrayList<>();
                for (Subscription s : subscriptions) {
                    String remoteAddr = subMapRemoteAddr.get(s);
                    if (!Objects.isNull(remoteAddr)) {
                        RoutedMessageAction data = RoutedMessageAction
                            .createFrom(message, s, remoteAddr);
                        result.add(data);
                    }
                }
                msg.replyTo.tell(new RoutedMessageActionResult(result));
            } catch (Exception e) {

            }
            return this;
        }).build();
    }

    public interface GroupSubscriptionAction extends AkkaDefaultSerializable {


        class UnRoutedMessage implements GroupSubscriptionAction {

            private Message message;

            private ActorRef<RoutedMessageActionResult> replyTo;

            public UnRoutedMessage(Message message) {
                this.message = message;
            }


            public UnRoutedMessage(Message message, ActorRef<RoutedMessageActionResult> replyTo) {
                this.message = message;
                this.replyTo = replyTo;
            }


            public Message getMessage() {
                return message;
            }

            public void setMessage(Message message) {
                this.message = message;
            }

            public ActorRef<RoutedMessageActionResult> getReplyTo() {
                return replyTo;
            }

            public void setReplyTo(
                ActorRef<RoutedMessageActionResult> replyTo) {
                this.replyTo = replyTo;
            }
        }


        class SubOrUnsubAction implements GroupSubscriptionAction {

            private Subscription subscription;
            private String remoteAddress;
            private boolean isSub;

            public SubOrUnsubAction(Subscription subscription, String remoteAddress,
                boolean isSub) {
                this.subscription = subscription;
                this.remoteAddress = remoteAddress;
                this.isSub = isSub;
            }

            public Subscription getSubscription() {
                return subscription;
            }

            public void setSubscription(Subscription subscription) {
                this.subscription = subscription;
            }

            public String getRemoteAddress() {
                return remoteAddress;
            }

            public void setRemoteAddress(String remoteAddress) {
                this.remoteAddress = remoteAddress;
            }

            public boolean isSub() {
                return isSub;
            }

            public void setSub(boolean sub) {
                isSub = sub;
            }
        }

    }


}
