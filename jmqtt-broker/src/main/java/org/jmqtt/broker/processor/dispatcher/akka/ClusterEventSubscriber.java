package org.jmqtt.broker.processor.dispatcher.akka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.model.AkkaDefaultSerializable;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent.DispatcherMsgEvent;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent.RegisterConsumerEvent;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.slf4j.Logger;

@Deprecated
public class ClusterEventSubscriber extends AbstractBehavior<ClusterEvent> {

    private static final Logger log = JmqttLogger.eventLog;

    private EventConsumeHandler eventConsumeHandler;


    private ClusterEventSubscriber(ActorContext<ClusterEvent> context,
        EventConsumeHandler eventConsumeHandler) {
        super(context);
        this.eventConsumeHandler = eventConsumeHandler;
    }

    public static Behavior<ClusterEvent> create(EventConsumeHandler eventConsumeHandler) {
        return Behaviors.setup(
            context -> new ClusterEventSubscriber(context, eventConsumeHandler)
        );
    }

    @Override
    public Receive<ClusterEvent> createReceive() {
        return newReceiveBuilder()
            .onMessage(ClusterEvent.DispatcherMsgEvent.class, this::onDispatcher)
            .build();
    }

    private Behavior<ClusterEvent> onDispatcher(DispatcherMsgEvent dispatcherMsgEvent) {
        this.eventConsumeHandler.consumeEvent(dispatcherMsgEvent.event);
        return this;
    }

    private Behavior<ClusterEvent> onRegisterConsumer(RegisterConsumerEvent registerConsumerEvent) {
        this.eventConsumeHandler = registerConsumerEvent.eventConsumeHandler;
        return this;
    }

    public interface ClusterEvent {

        class DispatcherMsgEvent implements ClusterEvent, AkkaDefaultSerializable {

            private Event event;

            public DispatcherMsgEvent(Event event) {
                this.event = event;
            }

            public Event getEvent() {
                return event;
            }

            public void setEvent(Event event) {
                this.event = event;
            }
        }

        class RegisterConsumerEvent implements ClusterEvent, AkkaDefaultSerializable {

            private EventConsumeHandler eventConsumeHandler;

            public RegisterConsumerEvent(EventConsumeHandler eventConsumeHandler) {
                this.eventConsumeHandler = eventConsumeHandler;
            }

            public EventConsumeHandler getEventConsumeHandler() {
                return eventConsumeHandler;
            }

            public void setEventConsumeHandler(
                EventConsumeHandler eventConsumeHandler) {
                this.eventConsumeHandler = eventConsumeHandler;
            }
        }
    }
}
