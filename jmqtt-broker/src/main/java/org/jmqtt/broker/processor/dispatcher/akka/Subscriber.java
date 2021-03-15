package org.jmqtt.broker.processor.dispatcher.akka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.slf4j.Logger;

public class Subscriber extends AbstractBehavior<Event> {

    private static final Logger log = JmqttLogger.eventLog;

    private EventConsumeHandler eventConsumeHandler;


    private Subscriber(ActorContext<Event> context, EventConsumeHandler eventConsumeHandler) {
        super(context);
        this.eventConsumeHandler = eventConsumeHandler;
    }

    public static Behavior<Event> create(EventConsumeHandler eventConsumeHandler) {
        return Behaviors.setup(context -> new Subscriber(context, eventConsumeHandler));
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder().onMessage(Event.class, this::onReceive).build();
    }

    private Behavior<Event> onReceive(Event event) {
        LogUtil.info(log, "[Subscriber:{}] onReceive:{}",getContext().getSelf().path(), event);
        this.eventConsumeHandler.consumeEvent(event);
        return this;
    }
}
