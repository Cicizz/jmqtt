package org.jmqtt.broker.processor.recover;

import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.helper.ThreadFactoryImpl;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.processor.HighPerformanceMessageHandler;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.SessionStore;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * send offline message and flow message when client re connect and cleanSession is false 设备重连：分发会话消息服务 客户端以新开始(Clean
 * Start)标志为0且会话存在的情况下重连时, 客户端和服务端都必须使用原始报文标识符重新发送任何未被确认的 PUBLISH 报文(当QoS > 0)和PUBREL报文. 这是唯一要求客户端 或服务端重发消息的情况. 客户端和服务端不能在其他任何时间重发消息
 */
public class ReSendMessageService extends HighPerformanceMessageHandler {

    private Logger log = JmqttLogger.messageTraceLog;

    private Thread                thread;
    private boolean               stoped  = false;
    private BlockingQueue<String> clients = new LinkedBlockingQueue<>();
    private int                   maxSize = 10000;
    private SessionStore          sessionStore;
    private MessageStore          messageStore;

    private ThreadPoolExecutor sendMessageExecutor = new ThreadPoolExecutor(4,
            4,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactoryImpl("ReSendMessageThread"));

    public ReSendMessageService(BrokerController brokerController) {
        super(brokerController);
        this.messageStore = brokerController.getMessageStore();
        this.sessionStore = brokerController.getSessionStore();
        this.thread = new Thread(new PutClient());
    }

    ;

    public boolean put(String clientId) {
        if (this.clients.size() > maxSize) {
            LogUtil.warn(log,"ReSend message busy! the client queue size is over {}", maxSize);
            return false;
        }
        this.clients.offer(clientId);
        return true;
    }

    public void wakeUp() {
        LockSupport.unpark(thread);
    }

    public void start() {
        thread.start();
    }

    public void shutdown() {
        if (!stoped) {
            stoped = true;
        }
    }

    // TODO 临时占位，需要代码优化
    interface Build{
        MqttMessage buildMqttMessage(Message message);
    }


    public boolean dispatcherMessage(String clientId,Message message, Build build) {
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        // client off line again
        if (clientSession == null) {
            LogUtil.warn(log,"The client offline again, put the message to the offline queue,clientId:{}", clientId);
            return false;
        }
        MqttMessage mqttMessage = build.buildMqttMessage(message);
        clientSession.getCtx().writeAndFlush(mqttMessage);
        return true;
    }

    class ResendMessageTask implements Callable<Boolean> {

        private String clientId;

        public ResendMessageTask(String clientId) {
            this.clientId = clientId;
        }

        @Override
        public Boolean call() {

            // 入栈报文：未处理的pubRec报文
            Collection<Message> waitPubRecMsgs = getAllInflowMsg(clientId);
            if (!MixAll.isEmpty(waitPubRecMsgs)) {
                for (Message waitPubRecMsg : waitPubRecMsgs) {
                    if (!dispatcherMessage(clientId, waitPubRecMsg, new Build() {
                        @Override
                        public MqttMessage buildMqttMessage(Message message) {
                            return MessageUtil.getPubRecMessage(message.getMsgId(),true);
                        }
                    })) {
                        LogUtil.warn(log,"ReSendMessageService resend inflow error,{}",waitPubRecMsg);
                    }
                }
            }

            Build publishMqttMsg = new Build() {
                @Override
                public MqttMessage buildMqttMessage(Message message) {
                    int qos = (int) message.getHeader(MessageHeader.QOS);
                    int messageId = message.getMsgId();
                    return MessageUtil.getPubMessage(message, false);
                }
            };

            // 出栈报文-qos1,qos2第一阶段：
            Collection<Message> outflowMsgs = getAllOutflowMsg(clientId);
            if (!MixAll.isEmpty(outflowMsgs)) {
                for (Message message : outflowMsgs) {
                    if (!dispatcherMessage(clientId, message, publishMqttMsg)) {
                        LogUtil.warn(log,"ReSendMessageService resend outflow error,{}",message);
                    }
                }
            }

            // 出栈报文-qos2第二阶段：
            Collection<Integer> qos2MsgIds = getAllOutflowSecMsgId(clientId);
            if (!MixAll.isEmpty(outflowMsgs)) {
                for (Integer msgId : qos2MsgIds) {
                    Message temp = new Message();
                    temp.setMsgId(msgId);
                    if (!dispatcherMessage(clientId, temp, new Build() {
                        @Override
                        public MqttMessage buildMqttMessage(Message message) {
                            return MessageUtil.getPubRelMessage(msgId);
                        }
                    })) {
                        return false;
                    }
                }
            }

            // 出栈消息：离线消息，未分发的publish消息
            Collection<Message> messages = sessionStore.getAllOfflineMsg(clientId);
            if (!MixAll.isEmpty(outflowMsgs)) {
                for (Message message : messages) {
                    if (!dispatcherMessage(clientId, message, publishMqttMsg)) {
                        return false;
                    }
                }
            }
            sessionStore.clearOfflineMsg(clientId);

            return true;
        }
    }

    class PutClient implements Runnable {
        @Override
        public void run() {
            while (!stoped) {
                if (clients.size() == 0) {
                    LockSupport.park(thread);
                }
                String clientId = clients.poll();
                ResendMessageTask resendMessageTask = new ResendMessageTask(clientId);
                long start = System.currentTimeMillis();
                try {
                    Boolean rs = sendMessageExecutor.submit(resendMessageTask).get(2000, TimeUnit.MILLISECONDS);
                    if (rs == null || !rs) {
                        LogUtil.warn(log,"ReSend message is interrupted,the client offline again,clientId={}", clientId);
                    }
                    long cost = System.currentTimeMillis() - start;
                    LogUtil.debug(log,"ReSend message clientId:{} cost time:{}", clientId, cost);
                } catch (Exception e) {
                    LogUtil.warn(log,"ReSend message failure,clientId:{}", clientId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
            LogUtil.info(log,"Shutdown re send message service success.");
        }
    }

}
