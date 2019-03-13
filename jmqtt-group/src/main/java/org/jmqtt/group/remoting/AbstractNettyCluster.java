package org.jmqtt.group.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.common.ClusterRemotingCommand;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.common.ResponseFuture;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNettyCluster {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private Map<Integer /* opaque */, ResponseFuture> responseTable = new ConcurrentHashMap<>();

    /**
     * Semaphore to limit maximum number of on-going asynchronous requests, which protects system memory footprint.
     */
    private Semaphore semaphore;

    public AbstractNettyCluster(){
        this.semaphore = new Semaphore(65536,true);
    }

    public AbstractNettyCluster(int semaphore){
        this.semaphore = new Semaphore(semaphore,true);
    }

    public void invokeAsync(final Channel channel, final ClusterRemotingCommand command, final long timeout, InvokeCallback invokeCallback){
        final int opaque = command.getOpaque();
        try{
            ResponseFuture responseFuture = new ResponseFuture(channel,opaque,timeout,invokeCallback);
            boolean tryAquired = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            if(tryAquired){
                responseTable.put(opaque,responseFuture);
                final String remotingAddr = RemotingHelper.getRemoteAddr(channel);
                channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            responseFuture.setSendRequestOK(true);
                            return;
                        }
                        requestFail(opaque);
                        log.warn("send a request command to channel <{}> failed.", remotingAddr);
                    }
                });
            }else{
                // TODO 获取失败应该放入重试队列进行重试
                log.warn("Async invoke aquire semaphore failure,waiting threadNums:{},semaphoreAsyncValue:{}",semaphore.getQueueLength(),semaphore.availablePermits());
            }
        }catch(Exception ex){
        }
    }

    private void requestFail(final int opaque){
        // TODO 发送失败，从缓存移除该future并放入重试任务队列

    }

    public void processMessageReceived(ChannelHandlerContext ctx, ClusterRemotingCommand cmd){

    }


}
