package org.jmqtt.group.remoting.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class NettyClusterDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private static final int FRAME_MAX_LENGTH = Integer.parseInt(System.getProperty("org.jmqtt.group.remoting.frameMaxLength","16777216"));

    public NettyClusterDecoder(){
        super(FRAME_MAX_LENGTH,0,4,0,4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try{
            frame = (ByteBuf) super.decode(ctx,in);
            if(null == frame){
                return null;
            }
            ByteBuffer byteBuffer = in.nioBuffer();
            return decode(byteBuffer);
        }catch(Exception ex){
            log.error("Decode exception,ex={}",ex);
            RemotingHelper.closeChannel(ctx.channel());
        }finally {
            if (null != frame){
                frame.release();
            }
        }
        return null;
    }

    private ClusterRemotingCommand decode(ByteBuffer byteBuffer){
        int length = byteBuffer.limit();
        int headerLength = byteBuffer.getInt();

        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);
        ClusterRemotingCommand cmd = JSONObject.parseObject(headerData,ClusterRemotingCommand.class);
        int bodyLength = length - headerLength;
        byte[] bodyData = null;
        if(bodyLength > 0){
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }
        cmd.setBody(bodyData);
        return cmd;
    }
}
