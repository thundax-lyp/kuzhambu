package com.thundax.kuzhambu.common.core.crypto;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RsaCrypto {

    private static final String ALGOR = "RSA";
    private static final String PROVIDER = "BC";
    private static final int KEY_SIZE = 1024;
    private static final String CLIPHER_TRANSFORMATION = "RSA/None/PKCS1Padding";
    private static final String CHARSET = "UTF-8";

    static {
        /**
         * 这里需使用BouncyCastleProvider，作为编解码的内核，原因是：
         * 客户单的jsencrypt.js在作编码的时候，使用了随机数，对同一个pubkey的每次编码，会产生不同的结果
         */
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成钥对，生成的数据存储在map<String, String>中 RsaCrypto.PUBKEY：base64编码的public key,用于发送给浏览器的jsencrypt.js
     * RsaCrypto.PUBKEY_MODULUS与RsaCrypto.PRIKEY_EXPONENT用于存储在Session中，以解码时使用
     */
    public static ReadableKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGOR, PROVIDER);
            generator.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String publicKeyText = new String(Base64.encodeBase64(publicKey.getEncoded()));
            String modulus = publicKey.getModulus().toString(16);
            String privateKeyExponent = privateKey.getPrivateExponent().toString(16);
            String publicKeyExponent = publicKey.getPublicExponent().toString(16);

            return new ReadableKeyPair(publicKeyText, modulus, publicKeyExponent, privateKeyExponent);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 解码，这里可能会存在加密数据过长的问题
     *
     * @param encryptBase64Text base64编码后的加密数据
     */
    public static String decryptBase64(String encryptBase64Text, ReadableKeyPair keyPair) {
        if (StringUtils.isBlank(encryptBase64Text) || keyPair == null || keyPair.getModulus() == null) {
            return StringUtils.EMPTY;
        }
        try {
            BigInteger modulus = new BigInteger(keyPair.getModulus(), 16);
            BigInteger privateKeyExponent = new BigInteger(keyPair.getPrivateKeyExponent(), 16);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGOR);
            RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateKeyExponent);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            Cipher cipher = Cipher.getInstance(CLIPHER_TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainText = cipher.doFinal(Base64.decodeBase64(encryptBase64Text));
            return new String(plainText, CHARSET);

        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public static class ReadableKeyPair implements Serializable {

        private String publicKey;

        private String modulus;

        private String privateKeyExponent;

        private String publicKeyExponent;

        public ReadableKeyPair(String publicKey, String modulus, String publicKeyExponent, String privateKeyExponent) {
            this.setPublicKey(publicKey);
            this.setModulus(modulus);
            this.setPublicKeyExponent(publicKeyExponent);
            this.setPrivateKeyExponent(privateKeyExponent);
        }

        public String getPublicKey() {
            return this.publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getModulus() {
            return this.modulus;
        }

        public void setModulus(String modulus) {
            this.modulus = modulus;
        }

        public String getPrivateKeyExponent() {
            return this.privateKeyExponent;
        }

        public void setPrivateKeyExponent(String privateKeyExponent) {
            this.privateKeyExponent = privateKeyExponent;
        }

        public String getPublicKeyExponent() {
            return this.publicKeyExponent;
        }

        public void setPublicKeyExponent(String publicKeyExponent) {
            this.publicKeyExponent = publicKeyExponent;
        }
    }
}
