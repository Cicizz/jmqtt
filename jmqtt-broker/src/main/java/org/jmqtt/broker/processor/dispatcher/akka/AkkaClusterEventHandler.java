package org.jmqtt.broker.processor.dispatcher.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.pubsub.Topic.Command;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.List;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.slf4j.Logger;

public class AkkaClusterEventHandler implements ClusterEventHandler {

    private static final Logger log = JmqttLogger.eventLog;
    private ActorRef<Command<Event>> topic;
    private ActorRef<Event> subscriber;
    private EventConsumeHandler eventConsumeHandler;

    public AkkaClusterEventHandler() {

    }

    @Override
    public void start(BrokerConfig brokerConfig) {
        Config config = ConfigFactory.load("akka");
        // Create an Akka system
        Behavior<Void> initBehavior = Behaviors.setup(
            context -> {
                topic =
                    context.spawn(Topic.create(Event.class, "jmqtt-event"), "JMqttEvent");
                subscriber = context
                    .spawn(Subscriber.create(this.eventConsumeHandler), "JMqttEventSubscriber");
                topic.tell(Topic.subscribe(subscriber));
                return Behaviors.empty();
            });
        ActorSystem.create(initBehavior, "JMqttSystem", config);

    }

    @Override
    public void shutdown() {
        topic.tell(Topic.unsubscribe(subscriber));

    }

    @Override
    public boolean sendEvent(Event event) {
        topic.tell(Topic.publish(event));
        return true;
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {
        this.eventConsumeHandler = eventConsumeHandler;
    }

    @Override
    public List<Event> pollEvent(int maxPollNum) {
        return null;
    }
}
