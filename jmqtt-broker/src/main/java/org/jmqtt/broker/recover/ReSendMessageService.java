package org.jmqtt.broker.recover;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.common.model.MessageHeader;
import org.jmqtt.remoting.session.ClientSession;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.OfflineMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * send offline message and flow message when client re connect and cleanSession is false
 */
public class ReSendMessageService {

    private Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    private Thread thread;
    private boolean stoped = false;
    private BlockingQueue<String> clients = new LinkedBlockingQueue<>();
    private OfflineMessageStore offlineMessageStore;
    private FlowMessageStore flowMessageStore;
    private int maxSize = 10000;
    private ThreadPoolExecutor sendMessageExecutor = new ThreadPoolExecutor(4,
            4,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactoryImpl("ReSendMessageThread"));


    public ReSendMessageService(OfflineMessageStore offlineMessageStore, FlowMessageStore flowMessageStore){
        this.offlineMessageStore = offlineMessageStore;
        this.flowMessageStore = flowMessageStore;
        this.thread = new Thread(new PutClient());
    };

    public boolean put(String clientId){
        if(this.clients.size() > maxSize){
            log.warn("ReSend message busy! the client queue size is over {}",maxSize);
            return false;
        }
        this.clients.offer(clientId);
        return true;
    }

    public void wakeUp(){
        LockSupport.unpark(thread);
    }

    public void start(){
        thread.start();
    }

    public void shutdown(){
        if(!stoped){
            stoped = true;
        }
    }

    public boolean dispatcherMessage(String clientId, Message message){
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        // client off line again
        if(clientSession == null){
            log.warn("The client offline again, put the message to the offline queue,clientId:{}",clientId);
            return false;
        }
        int qos = (int) message.getHeader(MessageHeader.QOS);
        int messageId = message.getMsgId();
        if(qos > 0){
            flowMessageStore.cacheSendMsg(clientId,message);
        }
        MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false,qos,messageId);
        clientSession.getCtx().writeAndFlush(publishMessage);
        return true;
    }

    class ResendMessageTask implements Callable<Boolean> {

        private String clientId;
        public ResendMessageTask(String clientId){
            this.clientId = clientId;
        }

        @Override
        public Boolean call() {
            Collection<Message> flowMsgs = flowMessageStore.getAllSendMsg(clientId);
            for(Message message : flowMsgs){
                if(!dispatcherMessage(clientId,message)){
                    return false;
                }
            }
            if(offlineMessageStore.containOfflineMsg(clientId)){
                Collection<Message> messages = offlineMessageStore.getAllOfflineMessage(clientId);
                for(Message message : messages){
                    if(!dispatcherMessage(clientId,message)){
                        return false;
                    }
                }
                offlineMessageStore.clearOfflineMsgCache(clientId);
            }
            return true;
        }
    }

    class PutClient implements Runnable{
        @Override
        public void run() {
            while (!stoped){
                if(clients.size() == 0){
                    LockSupport.park(thread);
                }
                String clientId = clients.poll();
                ResendMessageTask resendMessageTask = new ResendMessageTask(clientId);
                long start = System.currentTimeMillis();
                try {
                    boolean rs = sendMessageExecutor.submit(resendMessageTask).get(2000,TimeUnit.MILLISECONDS);
                    if(!rs){
                        log.warn("ReSend message is interrupted,the client offline again,clientId={}",clientId);
                    }
                    long cost = System.currentTimeMillis() - start;
                    log.debug("ReSend message clientId:{} cost time:{}",clientId,cost);
                } catch (Exception e) {
                    log.warn("ReSend message failure,clientId:{}",clientId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
            log.info("Shutdown re send message service success.");
        }
    }

}
