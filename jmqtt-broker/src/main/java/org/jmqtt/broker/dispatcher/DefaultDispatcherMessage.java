package org.jmqtt.broker.dispatcher;


import org.jmqtt.common.bean.Message;
import org.jmqtt.common.helper.RejectHandler;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
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

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private boolean stoped = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor pollThread;
    private int pollThreadNum;

    public DefaultDispatcherMessage(int pollThreadNum){
        this.pollThreadNum = pollThreadNum;
    }

    @Override
    public void start() {
        this.pollThread = new ThreadPoolExecutor(pollThreadNum,
                pollThreadNum,
                60*1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new ThreadFactoryImpl("pollMessage2Subscriber"),
                new RejectHandler("pollMessage",100000));

        new Thread(new Runnable() {
            @Override
            public void run() {
                int waitTime = 100;
                while(!stoped){
                    try {
                        List<Message> messageList = new ArrayList(32);
                        for(int i = 0; i < 32; i++){
                            Message message = messageQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                            if(Objects.nonNull(message)){
                                messageList.add(message);
                                waitTime = 100;
                            }else{
                                waitTime = 3000;
                            }
                        }
                        if(messageList.size() > 0){
                            AsyncDispatcher dispatcher = new AsyncDispatcher(messageList);
                            pollThread.submit(dispatcher);
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
        if(!isNotFull){
            log.warn("[PubMessage] -> the buffer queue is full");
        }
        return isNotFull;
    }


    @Override
    public void shutdown(){
        this.stoped = true;
    };

    class AsyncDispatcher implements Runnable{

        private List<Message> messages;

        public AsyncDispatcher(List<Message> messages){
            this.messages = messages;
        }

        @Override
        public void run() {
            //TODO 分发消息给订阅者

        }
    }
}
