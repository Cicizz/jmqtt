package org.jmqtt.broker.processor.dispatcher.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.pubsub.Topic.Command;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent;
@Deprecated
public interface AkkaActorListener {

    void notifyClusterEventTopicActor(ActorRef<Command<ClusterEvent>> actorRef);

    void notifyClusterEventActor(ActorRef<ClusterEvent> actorRef);


}
