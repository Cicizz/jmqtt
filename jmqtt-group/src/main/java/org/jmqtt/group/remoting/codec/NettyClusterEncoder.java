package org.jmqtt.group.remoting.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class NettyClusterEncoder extends MessageToByteEncoder<ClusterRemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ClusterRemotingCommand clusterRemotingCommand, ByteBuf out) throws Exception {
        try{
            ByteBuffer buffer = encode(clusterRemotingCommand);
            out.writeBytes(buffer);

        } catch (Exception ex){
            log.error("Encode cluster remoting message error,clusterRemotingCommand = {}",clusterRemotingCommand);
            RemotingHelper.closeChannel(channelHandlerContext.channel());
        }
    }

    private ByteBuffer encode(ClusterRemotingCommand cmd){
        // header length size
        int length = 4;

        // header data length
        String json = JSONObject.toJSONString(cmd,false);
        byte[] headerData = null;
        if(json != null){
            headerData = json.getBytes(CHARSET_UTF8);
        }
        length += headerData.length;

        // body data length
        int bodyLength = cmd.getBody() != null ? cmd.getBody().length : 0;
        length += bodyLength;

        // write data
        ByteBuffer result = ByteBuffer.allocate(4 + length);
        result.putInt(length);             // len(4 + headerdata + body)
        result.putInt(headerData.length);  // len(headerdata)
        result.put(headerData);            // headerdata
        if (cmd.getBody() != null){
            result.put(cmd.getBody());     // bodydata
        }

        // filp data for network transporting
        result.flip();
        return result;
    }
}
