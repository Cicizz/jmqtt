package org.jmqtt.group.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.helper.Pair;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.common.ResponseFuture;
import org.jmqtt.group.common.SemaphoreReleaseOnlyOnce;
import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.jmqtt.group.protocol.ClusterResponseCode;
import org.jmqtt.group.protocol.MessageFlag;
import org.jmqtt.remoting.exception.RemotingSendRequestException;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNettyCluster {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private final Map<Integer /* opaque */, ResponseFuture> responseTable = new ConcurrentHashMap<>();
    protected final ClusterReSendCommandService resendService = new ClusterReSendCommandService(this);
    protected final Map<Integer /* code */, Pair<ClusterRequestProcessor, ExecutorService>> processorTable = new ConcurrentHashMap<>();

    /**
     * Semaphore to limit maximum number of on-going asynchronous requests, which protects system memory footprint.
     */
    private Semaphore semaphore;

    public AbstractNettyCluster() {
        this.semaphore = new Semaphore(65536, true);
    }

    public AbstractNettyCluster(int semaphore) {
        this.semaphore = new Semaphore(semaphore, true);
    }

    public void scanResponseTable(){
        List<ResponseFuture> rsList = new LinkedList<>();
        Iterator<Map.Entry<Integer, ResponseFuture>> iterable = responseTable.entrySet().iterator();
        while(iterable.hasNext()){
            Map.Entry<Integer,ResponseFuture>  next = iterable.next();
            ResponseFuture responseFuture = next.getValue();
            if((responseFuture.getBeginTime()+responseFuture.getTimeoutMillis()+1000) <= System.currentTimeMillis()){
                responseFuture.release();
                iterable.remove();
                rsList.add(responseFuture);
                log.warn("remove time out response future");
            }
        }
        for(ResponseFuture rep : rsList){
            responseFail(rep);
        }

    }

    public void invokeAsyncImpl(final Channel channel, final ClusterRemotingCommand command, final long timeout, InvokeCallback invokeCallback) throws RemotingSendRequestException {
        final int opaque = command.getOpaque();
        try {
            SemaphoreReleaseOnlyOnce semaphoreReleaseOnlyOnce = new SemaphoreReleaseOnlyOnce(semaphore);
            ResponseFuture responseFuture = new ResponseFuture(channel, opaque, timeout, invokeCallback, semaphoreReleaseOnlyOnce);
            responseFuture.setClusterRemotingCommand(command);
            boolean tryAquired = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            if (tryAquired) {
                responseTable.put(opaque, responseFuture);
                final String remotingAddr = RemotingHelper.getRemoteAddr(channel);
                channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        }
                        requestFail(opaque,command);
                        log.warn("send a request command to channel <{}> failed.", remotingAddr);
                    }
                });
            } else {
                requestFail(opaque,command);
                log.warn("Async invoke aquire semaphore failure,waiting threadNums:{},semaphoreAsyncValue:{}", semaphore.getQueueLength(), semaphore.availablePermits());
            }
        } catch (Exception ex) {
            log.info("send request failure", ex);
            throw new RemotingSendRequestException("send request failure");
        }
    }

    protected void processMessageReceived(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        if (cmd != null) {
            if (MessageFlag.COMPRESSED_FLAG == cmd.getFlag()) {
                byte[] body = cmd.getBody();
                try {
                    body = MixAll.uncompress(body);
                    cmd.setBody(body);
                } catch (IOException e) {
                    log.info("uncompress cluster message failure", e);
                }
            }
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequest(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponse(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    private void processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        final Pair<ClusterRequestProcessor, ExecutorService> pair = this.processorTable.get(cmd.getCode());
        final int opaque = cmd.getOpaque();
        if(pair != null){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final ClusterRemotingCommand responseCommand = pair.getObject1().processRequest(ctx,cmd);
                    if(responseCommand != null){
                        responseCommand.setOpaque(opaque);
                        responseCommand.makeResponseType();
                        ctx.writeAndFlush(responseCommand).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if(!channelFuture.isSuccess()){
                                    log.warn("cluster transfer message failure,addr={}",RemotingHelper.getRemoteAddr(ctx.channel()));
                                }
                            }
                        });
                    }
                }
            };
            pair.getObject2().submit(runnable);
        } else {
            log.error("cluster request has no processor,code={}",cmd.getCode());
            ClusterRemotingCommand responseCommand = new ClusterRemotingCommand(ClusterResponseCode.REQUEST_CODE_NOT_SUPPORTED);
            responseCommand.setOpaque(opaque);
            responseCommand.makeResponseType();
            ctx.writeAndFlush(responseCommand);
        }
    }

    private void processResponse(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        final int opaque = cmd.getOpaque();
        final int code = cmd.getCode();
        // response failure , resend later
        if(code == ClusterResponseCode.RESPONSE_OK){
            final ResponseFuture responseFuture = responseTable.get(opaque);
            if (responseFuture != null){
                log.debug("receive response future, code={}, opaque={}.",cmd.getCode(), opaque);
                responseFuture.setClusterRemotingCommand(cmd);
                responseFuture.executeCallback();
                responseFuture.release();
            } else {
                log.warn("cluster receive not exist response future, code={}, opaque={}.",cmd.getCode(),opaque);
            }
        }else{
            log.warn("receive response is error,code={}",code);
        }
    }

    private void requestFail(final int opaque, final ClusterRemotingCommand command) {
        log.debug("command {} enqueue ClusterResendCommandQueue.",command);
        ResponseFuture responseFuture = responseTable.remove(opaque);
        responseFuture.release();
        resendService.appendMessage(
                responseFuture.getChannel(),
                command,
                responseFuture.getTimeoutMillis(),
                responseFuture.getInvokeCallback()
        );

    }

    private void responseFail(ResponseFuture responseFuture){
        ClusterRemotingCommand command = responseFuture.getClusterRemotingCommand();
        resendService.appendMessage(responseFuture.getChannel(),command,responseFuture.getTimeoutMillis(),responseFuture.getInvokeCallback());
    }

    protected void registerProcessor(int requestCode, ClusterRequestProcessor processor, ExecutorService service) {
        this.processorTable.put(requestCode, new Pair(processor, service));
    }


}
