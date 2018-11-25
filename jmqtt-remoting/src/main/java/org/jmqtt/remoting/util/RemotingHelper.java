package org.jmqtt.remoting.util;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class RemotingHelper {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING);

    public static void closeChannel(Channel channel){
        String remoteAddr = getRemoteAddr(channel);
        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("[closeChannel] -> close the connection,addr={},result={}",remoteAddr,channelFuture.isSuccess());
            }
        });
    }

    public static String getRemoteAddr(Channel channel){
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";
        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }
        return "";
    }


}
