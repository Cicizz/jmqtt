package com.jmqtt.mqtt.v3.acceptance.util;

import com.jmqtt.mqtt.v3.acceptance.model.MqttServer;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class MqttAclClientFactory extends MqttClientFactory{

    private static final Logger logger = LoggerFactory.getLogger(MqttAclClientFactory.class);

    public MqttAclClientFactory(MqttServer mqttServer) {
        super(mqttServer);
    }



    /**
     * About username and password,refering to https://testasia.atlassian.net/wiki/spaces/PI/pages/253329409/v1-Authentication
     *
     * @return
     */

    //TODO, should fix this method by runtime generated configuration
    @Deprecated
    public Mqtt5Client createTLSMqtt5Client() throws NoSuchAlgorithmException {
        return Mqtt5Client.builder()
            .serverHost(getMqttServer().getHost())
            .serverPort(getMqttServer().getPort())
            .identifier("TESTDSN_040942_000")
            .simpleAuth()
            .username("V1/PROOF")
            .password("lyQNcbvT8kpOvFEatn8wK0ioW40Jkg5nWNl9PfzpJZu1Ruj2yTEp0IeoLHOxUxFIJNSWGsne9o6h9+YMe++M5vLV2vqEYOby59o/Taezy35mv8LdZq3T3jPR4bYWYzsCc5GMbtNwUAKuveB7miGv3bO5oUP0NpNGjGClYA6fKMBoTjMxCez36lyI9DByRkSughTfZZFdvV+R6cqXKgF8PU5SoGgluKmq5p6eY4jr8LnY+B8k89OOuhpfk/rxeTGz6Xm/ykX+scbBPvebQMQH3xDcAIHg4Wo974LNzoMcpCTefVYQYoMSN9s/1sAI0diIrMl6g37SJPvBCbh8w/FGWg==".getBytes())
            .applySimpleAuth()
            .transportConfig()
            .sslConfig()
            .trustManagerFactory(MyTrustManagerFactory.getMyInstance())
            .applySslConfig()
            .applyTransportConfig()
            .build();

    }

    public static SocketFactory getTruststoreFactory() throws Exception {

        //Create key store

        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inKey = new FileInputStream("mqtt-paho-client-1.jks");
        keyStore.load(inKey, "your-client-key-store-password".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory
            .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "your-client-key-password".toCharArray());

        //Create trust store

        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream("mqtt-client-trust-store.jks");
        trustStore.load(in, "your-client-trust-store-password".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm());


        // Build SSL context

        SSLContext sslCtx = SSLContext.getInstance("TLSv1.2");
        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslCtx.getSocketFactory();

    }

    public static void connectAsDncInstance(Mqtt5Client theMqtt5Client,String dncUserName) {
        logger.info("DNC connected : clientId={}, username={}", theMqtt5Client.getConfig().getClientIdentifier(), dncUserName);
        theMqtt5Client.toBlocking()
                .connectWith()
                .cleanStart(true)
                .simpleAuth(Mqtt5SimpleAuth.builder().username(dncUserName).build())
                .sessionExpiryInterval(0)
                .send();
    }

    public static void connectTLSAsDncInstance(Mqtt5Client theMqtt5Client, String dncUserName) {
        logger.info("DNC connected : clientId={}, username={}", theMqtt5Client.getConfig().getClientIdentifier(), dncUserName);
        theMqtt5Client.toBlocking()
                .connectWith()
                .cleanStart(true)
                .sessionExpiryInterval(0)
                .send();
    }

    public static void connectAsDevice(Mqtt5Client theMqtt5Client) {
        logger.info("Device connected : clientId={}, username={}", theMqtt5Client.getConfig().getClientIdentifier(), "");
        theMqtt5Client.toBlocking()
                .connectWith()
                .cleanStart(true)
                .sessionExpiryInterval(30)
                .send();
    }

    //SSL related
    static class MyTrustManagerFactory extends TrustManagerFactory {
        protected MyTrustManagerFactory(TrustManagerFactorySpi factorySpi) throws NoSuchAlgorithmException {
            super(factorySpi, null, "");
        }

        public static final TrustManagerFactory getMyInstance() throws NoSuchAlgorithmException {
            return new MyTrustManagerFactory(new MyTrustManagerFactorySpi());
        }


    }

    public static class MyTrustManager implements TrustManager, X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    static class MyTrustManagerFactorySpi extends TrustManagerFactorySpi {
        public MyTrustManagerFactorySpi() {
            super();
        }

        @Override
        protected void engineInit(KeyStore keyStore) throws KeyStoreException {

        }

        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {

        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[]{new MyTrustManager()};
        }
    }

//    public static Function<Throwable,? extends Mqtt5SubAck> getDefaultSubscribeExceptionallyConsumer() {
//
//        return (Throwable ex) -> {
//            logger.error(ex.getMessage(), ex);
//            return new Mqtt5Sub
//        };
//
//    }
//
//
//    public static Function<Throwable,? extends Mqtt5PublishResult> getDefaultPublishExceptionallyConsumer() {
//    }
}
