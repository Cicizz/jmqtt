package org.jmqtt.broker.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.pubsub.Topic.Command;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.sql.Time;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.jmqtt.broker.processor.dispatcher.event.Event;

public class AkkaMain {

    public static void main(String[] args) {

        Config config = ConfigFactory.load("akka");
        // Create an Akka system
        Behavior<Void> initBehavior = Behaviors.setup(
            context -> {
                ActorRef<Command<Event>> topic =
                    context.spawn(Topic.create(Event.class, "jmqtt-event"), "JMqttEvent");
                ActorRef<Event> pubSub = context
                    .spawn(PubSubExample.create(new ConsumerExample()), "JMqttPubSub");

                topic.tell(Topic.subscribe(pubSub));

                for (int i = 0; i < 10; i++) {
                    TimeUnit.SECONDS.sleep(3);
                    Event event = new Event(new Random().nextInt(),
                        "xxx" + new Random().nextLong(),
                        new Random().nextLong(), "");
                    System.out.println("begin publish " + event);
                    topic.tell(Topic.publish(event));
                }

                return Behaviors.empty();
            });

        ActorSystem
            .create(initBehavior, "JMqttDispatcherSystem", config);
    }
}
