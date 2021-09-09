package org.jmqtt.broker.subscribe;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.pubsub.Topic.Command;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Message.Stage;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.InnerMessageDispatcher;
import org.jmqtt.broker.processor.dispatcher.akka.AkkaActorListener;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber;
import org.jmqtt.broker.processor.dispatcher.akka.ClusterEventSubscriber.ClusterEvent;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction;
import org.jmqtt.broker.subscribe.GroupMessageHandler.GroupMessageAction.RoutedMessageActionResult;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction.SubOrUnsubAction;
import org.jmqtt.broker.subscribe.GroupSubscriberCollector.GroupSubscriptionAction.UnRoutedMessage;
import org.slf4j.Logger;

public class AkkaController implements GroupSubscriptionAndMessageListener {

    private static final Logger log = JmqttLogger.messageTraceLog;
    /**
     * 当前节点地址
     */
    private static String remoteAddress;
    private ActorSystem<SpawnProtocol.Command> system;
    private ActorRef<GroupSubscriptionAction> subscriberCollector = null;
    private ActorRef<GroupMessageAction> messageHandler = null;
    private ActorRef<Command<GroupSubscriptionAction>> subscriptionActionTopic;
    private ActorRef<Command<GroupMessageAction>> messageTopic;
    private ActorRef<ClusterEvent> clusterEventSubscriber = null;
    private ActorRef<Command<ClusterEvent>> clusterEventTopic;
    private AkkaActorListener akkaActorListener;


    public AkkaController() {

    }

    public AkkaController(AkkaActorListener akkaActorListener) {
        this.akkaActorListener = akkaActorListener;
    }

    public ActorSystem<SpawnProtocol.Command> getSystem() {
        return system;
    }

    public void start(BrokerConfig brokerConfig, InnerMessageDispatcher innerMessageDispatcher,
        EventConsumeHandler eventConsumeHandler) {
        Config config = ConfigFactory.load(brokerConfig.getAkkaConfigName());
        Behavior<SpawnProtocol.Command> initBehavior = Behaviors.setup(
            context -> {

                //集群事件处理器配置
                //TODO 初始化优化
                //TODO 无论用以下方式还是AskPattern.ask方式创建的topic和subscriber，无法达到pub/sub的效果。
                if (!Objects.isNull(akkaActorListener)) {
                    clusterEventTopic =
                        context.spawn(Topic.create(ClusterEvent.class, "JMqttTopicClusterEvent"),
                            "JMqttTopicClusterEvent");
                    clusterEventSubscriber = context
                        .spawn(ClusterEventSubscriber.create(eventConsumeHandler),
                            "JMqttClusterEventSubscriber");

                    clusterEventTopic.tell(Topic.subscribe(clusterEventSubscriber));
                    akkaActorListener.notifyClusterEventActor(clusterEventSubscriber);
                    akkaActorListener.notifyClusterEventTopicActor(clusterEventTopic);
                }

                //共享订阅配置
                subscriptionActionTopic =
                    context
                        .spawn(Topic
                                .create(GroupSubscriptionAction.class,
                                    "JMqttTopicGroupSubscriptionAction"),
                            "JMqttTopicGroupSubscriptionAction");
                messageTopic =
                    context
                        .spawn(Topic
                                .create(GroupMessageAction.class, "JMqttTopicGroupMessage"),
                            "JMqttTopicGroupMessage");
                messageHandler = context
                    .spawn(GroupMessageHandler.create(innerMessageDispatcher,
                        context.getSystem().address().toString()),
                        "JMqttGroupMessageHandler");
                subscriberCollector = context
                    .spawn(GroupSubscriberCollector
                            .create(context.getSystem().address().toString(), messageHandler),
                        "JMqttGroupSubscriberCollector");

                messageTopic.tell(Topic.subscribe(messageHandler));
                subscriptionActionTopic.tell(Topic.subscribe(subscriberCollector));

                return SpawnProtocol.create();
            });

        system = ActorSystem.create(initBehavior, "JMqttSystem", config);
        remoteAddress = system.address().toString();

    }


    @Override
    public void subscribe(Subscription subscription) {
        sendEvent(subscription, true);

    }

    @Override
    public void unSubscribe(String topic, String clientId) {
        sendEvent(new Subscription(clientId, topic, 1), false);
    }

    @Override
    public void receiveNewMessage(Message message) {
        message.setStage(Stage.GROUP_DISPATHER);

        //根据共享订阅者所在的节点地址
        CompletionStage<RoutedMessageActionResult> completionStage = AskPattern.ask(
            subscriberCollector,
            replyTo -> new UnRoutedMessage(message, replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

        completionStage.whenCompleteAsync((result, err) -> {
            if (null == err) {
                result.messages.forEach(msg -> {
                    //路由至共享订阅者所在的节点地址
                    //TODO 无法直接通过"地址+路径"获取ActorSelection直接路由到连接订阅者的节点，暂时以pub/sub方式让全部节点接受，再根据地址判断是否处理
                    messageTopic.tell(Topic.publish(msg));
                });
            }
        });

    }

    private void sendEvent(Subscription subscription, boolean isSub) {
        subscriptionActionTopic.tell(
            Topic
                .publish(
                    new SubOrUnsubAction(subscription, remoteAddress, isSub)));

    }

}
