package org.jmqtt.remoting.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.*;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class NettySslHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING);

    public static ChannelHandler getSslHandler(SocketChannel channel, boolean useClientCA, String sslKeyStoreType, String sslKeyFilePath, String sslManagerPwd, String sslStorePwd) {

        SslContext sslContext = createSSLContext(useClientCA, sslKeyStoreType, sslKeyFilePath, sslManagerPwd, sslStorePwd);
        SSLEngine sslEngine = sslContext.newEngine(
                channel.alloc(),
                channel.remoteAddress().getHostString(),
                channel.remoteAddress().getPort());
        sslEngine.setUseClientMode(false); // server mode
        if (useClientCA) {
            sslEngine.setNeedClientAuth(true);
        }
        return new SslHandler(sslEngine);
    }

    private static SslContext createSSLContext(boolean useClientCA, String sslKeyStoreType, String sslKeyFilePath, String sslManagerPwd, String sslStorePwd) {
        try {
            InputStream ksInputStream = new FileInputStream(sslKeyFilePath);
            KeyStore ks = KeyStore.getInstance(sslKeyStoreType);
            ks.load(ksInputStream, sslStorePwd.toCharArray());


            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, sslManagerPwd.toCharArray());
            SslContextBuilder contextBuilder = SslContextBuilder.forServer(kmf);

            // whether need client CA(two-way authentication)
            if (useClientCA) {
                contextBuilder.clientAuth(ClientAuth.REQUIRE);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                contextBuilder.trustManager(tmf);
            }
            return contextBuilder.sslProvider(SslProvider.valueOf("JDK")).build();
        } catch (Exception ex) {
            log.error("Create ssl context failure.cause={}", ex);
            return null;
        }
    }

}
