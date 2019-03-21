package org.jmqtt.broker.dispatcher;


import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.remoting.session.ClientSession;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.helper.RejectHandler;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.OfflineMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DefaultDispatcherMessage implements MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private boolean stoped = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor pollThread;
    private int pollThreadNum;
    private SubscriptionMatcher subscriptionMatcher;
    private FlowMessageStore flowMessageStore;
    private OfflineMessageStore offlineMessageStore;

    public DefaultDispatcherMessage(int pollThreadNum, SubscriptionMatcher subscriptionMatcher, FlowMessageStore flowMessageStore, OfflineMessageStore offlineMessageStore) {
        this.pollThreadNum = pollThreadNum;
        this.subscriptionMatcher = subscriptionMatcher;
        this.flowMessageStore = flowMessageStore;
        this.offlineMessageStore = offlineMessageStore;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
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

    ;

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
                                    flowMessageStore.cacheSendMsg(clientId, message);
                                }
                                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, messageId);
                                clientSession.getCtx().writeAndFlush(publishMessage);
                            } else {
                                offlineMessageStore.addOfflineMessage(clientId, message);
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
