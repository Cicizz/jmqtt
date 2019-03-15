package org.jmqtt.group.common;

import io.netty.channel.Channel;
import org.jmqtt.group.protocol.ClusterRemotingCommand;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * cache request until receive response or store response to the local storage
 */
public class ResponseFuture {
    private final int opaque;
    private Channel channel;
    private final long timeoutMillis;
    private final long beginTime = System.currentTimeMillis();
    private volatile ClusterRemotingCommand clusterRemotingCommand;
    private volatile Throwable cause;
    private final InvokeCallback invokeCallback;
    private volatile boolean sendRequestOK = false;
    private final SemaphoreReleaseOnlyOnce semaphoreReleaseOnlyOnce;
    /**
     * callbackFlag is true show that callback has invoked. no longer to excute it
     */
    private AtomicBoolean callbackFlag = new AtomicBoolean(false);

    public ResponseFuture(Channel channel,int opaque,long timeoutMillis,InvokeCallback invokeCallback,SemaphoreReleaseOnlyOnce semaphoreReleaseOnlyOnce){
        this.channel = channel;
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.semaphoreReleaseOnlyOnce = semaphoreReleaseOnlyOnce;
    }

    public void executeCallback(){
        if(invokeCallback != null){
            if(callbackFlag.compareAndSet(false,true)){
                this.invokeCallback.invokeComplete(this);
            }
        }
    }

    public void release(){
        if(this.semaphoreReleaseOnlyOnce != null){
            this.semaphoreReleaseOnlyOnce.release();
        }
    }

    public int getOpaque() {
        return opaque;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public ClusterRemotingCommand getClusterRemotingCommand() {
        return clusterRemotingCommand;
    }

    public void setClusterRemotingCommand(ClusterRemotingCommand clusterRemotingCommand) {
        this.clusterRemotingCommand = clusterRemotingCommand;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public AtomicBoolean getCallbackFlag() {
        return callbackFlag;
    }

    public void setCallbackFlag(AtomicBoolean callbackFlag) {
        this.callbackFlag = callbackFlag;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    @Override
    public String toString() {
        return "ResponseFuture{" +
                "opaque=" + opaque +
                ", channel=" + channel +
                ", timeoutMillis=" + timeoutMillis +
                ", beginTime=" + beginTime +
                ", clusterRemotingCommand=" + clusterRemotingCommand +
                ", cause=" + cause +
                ", invokeCallback=" + invokeCallback +
                ", callbackFlag=" + callbackFlag +
                '}';
    }
}
