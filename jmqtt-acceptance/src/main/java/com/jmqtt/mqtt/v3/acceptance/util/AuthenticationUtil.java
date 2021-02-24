package com.jmqtt.mqtt.v3.acceptance.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public class AuthenticationUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUtil.class);



    /***
     * To generate the proof
     * @param publicKeyPem
     * @param dsn
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     */
    public static String getProof(String dsn, String publicKeyPem)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {

        long timeInSec = System.currentTimeMillis()/1000;
        String textToEncrypt = timeInSec + " GET /dsns/"+dsn+".json";
        logger.debug("textToEncrypt : {}" , textToEncrypt);

        return encryptWithtestRule(publicKeyPem, textToEncrypt);
    }

    public static String getOemSecret(String publicKeyPem, String oemKey, String oemId, String oemModel)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {

        long timeInSec = System.currentTimeMillis()/1000;
        String textToEncrypt = StringUtils.join(new Object[]{timeInSec, oemKey, oemId, oemModel}, " ");


        return encryptWithtestRule(publicKeyPem, textToEncrypt);
    }


    private static String encryptWithtestRule(String publicKeyPem, final String theTextToEncrypt)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        publicKeyPem = convertPKCS1ToPKCS8(publicKeyPem);

        publicKeyPem = publicKeyPem.replace("\n", "");
        publicKeyPem = publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyPem = publicKeyPem.replace("-----END PUBLIC KEY-----", "");

        byte[] publickeyInBytes = Base64.getDecoder().decode(publicKeyPem);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publickeyInBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicSpec);

        byte[] encrypted = encrypt(publicKey, theTextToEncrypt.getBytes());
        String proof = Base64.getEncoder().encodeToString(encrypted);

        logger.debug("Proof = {}", proof );
        return proof;
    }

    private static byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    private static String convertPKCS1ToPKCS8(String pkcs1) throws IOException {
        String uuid = UUID.randomUUID().toString();
        File pkcs1File = new File("keys/"+ uuid+"/pkcs1.key");
        File pkcs8File = new File("keys/"+uuid+"/pkcs8.key");
        String transformCmd = "openssl rsa  -RSAPublicKey_in -in " + pkcs1File.toPath() + " -out " + pkcs8File.toPath();
        logger.info("exec script : {}", transformCmd);
        FileUtils.writeStringToFile(pkcs1File, pkcs1, "UTF-8");
        Process p = Runtime.getRuntime().exec(transformCmd);

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s;
        while ((s = stdError.readLine()) != null) {
            if(! s.contains("writing RSA key")){
                logger.error("Here is the standard error of the command (if any):\n");
                logger.error(s);
                throw new IOException(s);
            }

        }

        return FileUtils.readFileToString(pkcs8File, "UTF-8");


    }


}
