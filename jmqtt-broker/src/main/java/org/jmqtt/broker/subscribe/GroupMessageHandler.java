package org.jmqtt.broker.subscribe;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.List;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.AkkaDefaultSerializable;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.processor.dispatcher.InnerMessageDispatcher;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction.RoutedMessageAction;
import org.slf4j.Logger;

public class GroupMessageHandler extends AbstractBehavior<GroupMessageAction> {

    private static final Logger log = JmqttLogger.messageTraceLog;
    private InnerMessageDispatcher innerMessageDispatcher;

    private String remoteAddress = null;

    private GroupMessageHandler(ActorContext<GroupMessageAction> context,
        InnerMessageDispatcher innerMessageDispatcher, String remoteAddress) {
        super(context);
        this.innerMessageDispatcher = innerMessageDispatcher;
        this.remoteAddress = remoteAddress;
    }

    public static Behavior<GroupMessageAction> create(
        InnerMessageDispatcher innerMessageDispatcher, String remoteAddress) {
        return Behaviors.setup(
            context ->
                new GroupMessageHandler(context, innerMessageDispatcher, remoteAddress)
        );
    }

    @Override
    public Receive<GroupMessageAction> createReceive() {
        return newReceiveBuilder()
            .onMessage(RoutedMessageAction.class, routedMessage -> {
                try {
                    if (remoteAddress.equals(routedMessage.remoteAddress)) {
                        Message message = routedMessage.getMessage();
                        innerMessageDispatcher.appendMessage(message);
                    }
                } catch (Exception e) {

                }
                return this;
            }).build();
    }


    public interface GroupMessageAction extends AkkaDefaultSerializable {

        class RoutedMessageAction implements GroupMessageAction {

            private Message message;

            private String remoteAddress;

            public RoutedMessageAction(Message message, String remoteAddress) {
                this.message = message;
                this.remoteAddress = remoteAddress;
            }

            public static RoutedMessageAction createFrom(Message message,
                Subscription dispatcher, String remoteAddress) {
                Message msg = new Message();
                msg.setDispatcher(dispatcher);
                msg.setStage(message.getStage());
                msg.setHeaders(message.getHeaders());
                msg.setPayload(message.getPayload());
                msg.setClientId(message.getClientId());
                msg.setMsgId(message.getMsgId());
                msg.setType(message.getType());
                msg.setStoreTime(message.getStoreTime());
                return new RoutedMessageAction(msg, remoteAddress);

            }

            public String getRemoteAddress() {
                return remoteAddress;
            }

            public void setRemoteAddress(String remoteAddress) {
                this.remoteAddress = remoteAddress;
            }

            public Message getMessage() {
                return message;
            }

            public void setMessage(Message message) {
                this.message = message;
            }
        }

        class RoutedMessageActionResult implements GroupMessageAction {

            List<RoutedMessageAction> messages;

            public RoutedMessageActionResult(List<RoutedMessageAction> messages) {
                this.messages = messages;
            }

            public List<RoutedMessageAction> getMessages() {
                return messages;
            }

            public void setMessages(
                List<RoutedMessageAction> messages) {
                this.messages = messages;
            }
        }


    }


}
