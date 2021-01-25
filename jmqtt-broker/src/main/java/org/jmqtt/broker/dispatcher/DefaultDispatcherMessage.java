package org.jmqtt.broker.dispatcher;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.broker.common.helper.RejectHandler;
import org.jmqtt.broker.common.helper.ThreadFactoryImpl;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 默认的消息分发实现类
 */
public class DefaultDispatcherMessage implements MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private boolean stoped = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor pollThread;
    private int pollThreadNum;
    private SubscriptionMatcher subscriptionMatcher;
    private SessionStore sessionStore;


    public DefaultDispatcherMessage(int pollThreadNum, SubscriptionMatcher subscriptionMatcher, SessionStore sessionStore) {
        this.pollThreadNum = pollThreadNum;
        this.subscriptionMatcher = subscriptionMatcher;
    }

    @Override
    public void start() {
        this.pollThread = new ThreadPoolExecutor(pollThreadNum,
                pollThreadNum,
                60 * 1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new ThreadFactoryImpl("pollMessage2Subscriber"),
                new RejectHandler("pollMessage", 100000));

        new Thread(() -> {
            int waitTime = 1000;
            while (!stoped) {
                try {
                    List<Message> messageList = new ArrayList(32);
                    Message message;
                    for (int i = 0; i < 32; i++) {
                        if (i == 0) {
                            message = messageQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                        } else {
                            message = messageQueue.poll();
                        }
                        if (Objects.nonNull(message)) {
                            messageList.add(message);
                        } else {
                            break;
                        }
                    }
                    if (messageList.size() > 0) {
                        AsyncDispatcher dispatcher = new AsyncDispatcher(messageList);
                        pollThread.submit(dispatcher).get();
                    }
                } catch (InterruptedException e) {
                    log.warn("poll message wrong.");
                } catch (ExecutionException e) {
                    log.warn("AsyncDispatcher get() wrong.");
                }
            }
        }).start();
    }

    @Override
    public boolean appendMessage(Message message) {
        boolean isNotFull = messageQueue.offer(message);
        if (!isNotFull) {
            log.warn("[PubMessage] -> the buffer queue is full");
        }
        return isNotFull;
    }

    @Override
    public void shutdown() {
        this.stoped = true;
        this.pollThread.shutdown();
    }

    class AsyncDispatcher implements Runnable {

        private List<Message> messages;

        AsyncDispatcher(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public void run() {
            if (Objects.nonNull(messages)) {
                try {
                    for (Message message : messages) {
                        Set<Subscription> subscriptions = subscriptionMatcher.match((String) message.getHeader(MessageHeader.TOPIC));
                        for (Subscription subscription : subscriptions) {
                            String clientId = subscription.getClientId();
                            ClientSession clientSession = ConnectManager.getInstance().getClient(subscription.getClientId());
                            if (ConnectManager.getInstance().containClient(clientId)) {
                                int qos = MessageUtil.getMinQos((int) message.getHeader(MessageHeader.QOS), subscription.getQos());
                                int messageId = clientSession.generateMessageId();
                                message.putHeader(MessageHeader.QOS, qos);
                                message.setMsgId(messageId);
                                if (qos > 0) {
                                    sessionStore.cacheOutflowMsg(clientId, message);
                                }
                                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, messageId);
                                clientSession.getCtx().writeAndFlush(publishMessage);
                            } else {
                                sessionStore.storeOfflineMsg(clientId, message);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Dispatcher message failure,cause={}", ex);
                }
            }
        }

    }
}
