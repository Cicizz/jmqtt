package org.jmqtt.broker.akka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.jmqtt.broker.processor.dispatcher.event.Event;

public class PubSubExample extends AbstractBehavior<Event> {


    private ConsumerExample comsumer;


    private PubSubExample(ActorContext<Event> context, ConsumerExample comsumer) {
        super(context);
        this.comsumer = comsumer;
    }

    public static Behavior<Event> create( ConsumerExample comsumer) {
        return Behaviors.setup(context -> {
            return new PubSubExample(context, comsumer);
        });
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder().onMessage(Event.class, this::onReceive).build();
    }

    private Behavior<Event> onReceive(Event event) {
        this.comsumer.consume(event);
        return this;
    }
}
