package org.jmqtt.group.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.junit.Test;

public class ClusterEncodingTest {
    NettyClusterEncoder nettyClusterEncoder = new NettyClusterEncoder();
    NettyClusterDecoder nettyClusterDecoder = new NettyClusterDecoder();

    @Test
    public void s222() throws NoSuchMethodException {
        ClusterRemotingCommand cmd = new ClusterRemotingCommand(1);
        cmd.setFlag(0);
        cmd.makeResponseType();
        cmd.setOpaque(1);
        cmd.setBody(new byte[]{116,112,123,46,53,12,112,113,89,5,101,123,41});

        ByteBuf out= Unpooled.buffer();
        PrivateMethodTestUtils.invoke(
                new NettyClusterEncoder(),
                "encode",
                new Class[]{ChannelHandlerContext.class, ClusterRemotingCommand.class, ByteBuf.class},
                new Object[]{null,cmd,out});

        ByteBuf in = out;

        ClusterRemotingCommand cmd2 = (ClusterRemotingCommand) PrivateMethodTestUtils.invoke(
                new NettyClusterDecoder(),
                "decode",
                new Class[]{ChannelHandlerContext.class, ByteBuf.class},
                new Object[]{null,in});
        assert cmd2.equals(cmd);
    }
}