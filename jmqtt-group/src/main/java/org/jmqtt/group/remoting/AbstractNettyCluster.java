package org.jmqtt.group.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.helper.Pair;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.common.ResponseFuture;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNettyCluster {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private final Map<Integer /* opaque */, ResponseFuture> responseTable = new ConcurrentHashMap<>();
    private final Map<Integer /* code */, Pair<ClusterRemotingCommand, ExecutorService>> processorTable = new ConcurrentHashMap<>();

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
            semaphore.release();
        }catch(Exception ex){
        }
    }

    protected void processMessageReceived(ChannelHandlerContext ctx, ClusterRemotingCommand cmd){
        if(cmd != null){
            switch (cmd.getType()){
                case REQUEST_COMMAND:
                    processRequest(ctx,cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponse(ctx,cmd);
                    break;
                default:
                    break;
            }
        }
    }

    private void processRequest(ChannelHandlerContext ctx,ClusterRemotingCommand cmd){

    }

    private void processResponse(ChannelHandlerContext ctx,ClusterRemotingCommand cmd){

    }

    private void requestFail(final int opaque){
        // TODO 发送失败，从缓存移除该future并放入重试任务队列

    }

    protected void registerProcessor(int requestCode, ClusterRequestProcessor processor,ExecutorService service){
        this.processorTable.put(requestCode,new Pair(processor,service));
    }


}
