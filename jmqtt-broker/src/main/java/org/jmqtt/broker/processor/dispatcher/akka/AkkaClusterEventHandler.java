package org.jmqtt.broker.processor.dispatcher.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.pubsub.Topic.Command;
import java.util.List;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent.DispatcherMsgEvent;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.slf4j.Logger;

@Deprecated
public class AkkaClusterEventHandler implements ClusterEventHandler, AkkaActorListener {

    private static final Logger log = JmqttLogger.eventLog;
    private ActorRef<Command<ClusterEvent>> topic;
    private ActorRef<ClusterEvent> subscriber;

    public AkkaClusterEventHandler() {

    }

    @Override
    public void start(BrokerConfig brokerConfig) {
        //与共享订阅akka共同初始化
    }

    @Override
    public void shutdown() {
        topic.tell(Topic.unsubscribe(subscriber));

    }

    @Override
    public boolean sendEvent(Event event) {
        topic.tell(Topic.publish(new DispatcherMsgEvent(event)));
        return true;
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {
    }

    @Override
    public List<Event> pollEvent(int maxPollNum) {
        return null;
    }

    @Override
    public void notifyClusterEventTopicActor(ActorRef<Command<ClusterEvent>> actorRef) {
        this.topic = actorRef;

    }

    @Override
    public void notifyClusterEventActor(ActorRef<ClusterEvent> actorRef) {
        this.subscriber = actorRef;

    }
}
