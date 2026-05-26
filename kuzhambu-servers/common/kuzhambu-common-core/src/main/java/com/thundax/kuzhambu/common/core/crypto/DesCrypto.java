package com.thundax.kuzhambu.common.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DesCrypto {

    private static final String ALGORITHM_DES = "DES";
    private static final Logger LOGGER = LoggerFactory.getLogger(DesCrypto.class);

    private DesCrypto() {}

    public static byte[] encrypt(byte[] plainBytes, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec keySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, key, random);
            return cipher.doFinal(plainBytes);

        } catch (InvalidKeyException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.warn("can not encrypt des value", e);
        }
        return null;
    }

    public static byte[] decrypt(byte[] encryptedBytes, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec keySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.DECRYPT_MODE, key, random);
            return cipher.doFinal(encryptedBytes);

        } catch (InvalidKeyException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.warn("can not decrypt des value", e);
        }
        return null;
    }

    public static String encrypt(String dataSource, String password) {
        byte[] bytes = encrypt(dataSource.getBytes(), password.getBytes(StandardCharsets.UTF_8));
        if (bytes != null) {
            return Hex.encodeHexString(bytes);
        }
        return null;
    }

    public static String decrypt(String hexText, String password) {
        try {
            byte[] bytes = decrypt(Hex.decodeHex(hexText), password.getBytes(StandardCharsets.UTF_8));
            if (bytes != null) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (DecoderException e) {
            LOGGER.warn("can not decode des hex text", e);
        }
        return null;
    }
}
