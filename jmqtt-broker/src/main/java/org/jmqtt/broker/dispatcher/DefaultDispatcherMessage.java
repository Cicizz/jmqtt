package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.helper.RejectHandler;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.OfflineMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DefaultDispatcherMessage implements MessageDispatcher {

    private static final Logger                 log          = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private boolean                             stoped       = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor                  pollThread;
    private int                                 pollThreadNum;
    private SubscriptionMatcher                 subscriptionMatcher;
    private FlowMessageStore                    flowMessageStore;
    private OfflineMessageStore                 offlineMessageStore;

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

                        }
                    } catch (InterruptedException e) {
                        log.warn("poll message wrong.");
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
}
