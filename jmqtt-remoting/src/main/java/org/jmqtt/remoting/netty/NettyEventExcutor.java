package org.jmqtt.remoting.netty;

import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NettyEventExcutor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING);

    private LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<>();
    private final int maxSize = 100000;
    private ChannelEventListener listener;
    boolean stoped = false;
    private Thread thread;

    public NettyEventExcutor(ChannelEventListener channelEventListener){
        this.listener = channelEventListener;
    }

    public void putNettyEvent(final NettyEvent nettyEvent){
        if (this.eventQueue.size() <= maxSize) {
            this.eventQueue.add(nettyEvent);
        } else {
            log.warn("[NettyEvent] -> event queue size[{}] enough, so drop this event {}", this.eventQueue.size(), nettyEvent.toString());
        }
    }

    @Override
    public void run() {
        while(!this.stoped){
            try{
                NettyEvent nettyEvent = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                if(nettyEvent != null && listener != null){
                    switch (nettyEvent.getEventType()){
                        case CONNECT:
                            listener.onChannelConnect(nettyEvent.getRemoteAddr(),nettyEvent.getChannel());
                            break;
                        case CLOSE:
                            listener.onChannelClose(nettyEvent.getRemoteAddr(),nettyEvent.getChannel());
                            break;
                        case EXCEPTION:
                            listener.onChannelException(nettyEvent.getRemoteAddr(),nettyEvent.getChannel());
                            break;
                        case IDLE:
                            listener.onChannelIdle(nettyEvent.getRemoteAddr(),nettyEvent.getChannel());
                            break;
                         default:
                             break;
                    }
                }
            }catch(Throwable t){
                log.warn("[NettyEvent] -> service has exception. ", t);
            }
        }
        log.info("[NettyEvent] -> NettyEventExcutor service end");
    }

    public void start(){
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void shutdown(){
        this.stoped = true;
    }
}
