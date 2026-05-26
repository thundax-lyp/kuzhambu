package com.thundax.kuzhambu.common.core.crypto;

import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sm4Crypto {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ENCODING = "UTF-8";
    private static final String ALGORITHM_NAME = "SM4";
    private static final Logger LOGGER = LoggerFactory.getLogger(Sm4Crypto.class);
    private static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";

    private Sm4Crypto() {}

    public static String encryptEcb(String key, String paramStr) {
        try {
            String cipherText = "";
            String hexKey = generateKey(key);
            byte[] keyData = decodeHex(hexKey);
            byte[] srcData = paramStr.getBytes(ENCODING);
            byte[] cipherArray = encryptEcbPadding(keyData, srcData);
            cipherText = Hex.encodeHexString(cipherArray);
            return cipherText;

        } catch (Exception e) {
            LOGGER.warn("can not encrypt sm4 ecb value", e);
            return null;
        }
    }

    private static byte[] encryptEcbPadding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static String decryptEcb(String key, String cipherText) {
        try {
            String hexKey = generateKey(key);
            String decryptStr = "";
            byte[] keyData = decodeHex(hexKey);
            byte[] cipherData = decodeHex(cipherText);
            byte[] srcData = decryptEcbPadding(keyData, cipherData);
            decryptStr = new String(srcData, ENCODING);
            return decryptStr;
        } catch (Exception e) {
            LOGGER.warn("can not decrypt sm4 ecb value", e);
            return null;
        }
    }

    private static byte[] decryptEcbPadding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    private static String generateKey(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        str = str + "2GOVCms";
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        if (sb.toString().trim().length() > 32) {
            return sb.toString().trim().substring(0, 32);
        }

        return sb.toString().trim();
    }

    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    public static boolean verifyEcb(String key, String cipherText, String paramStr) throws Exception {
        String hexKey = generateKey(key);
        boolean flag = false;
        byte[] keyData = decodeHex(hexKey);
        byte[] cipherData = decodeHex(cipherText);
        byte[] decryptData = decryptEcbPadding(keyData, cipherData);
        byte[] srcData = paramStr.getBytes(ENCODING);
        flag = Arrays.equals(decryptData, srcData);
        return flag;
    }

    private static byte[] decodeHex(String hexText) throws DecoderException {
        return Hex.decodeHex(hexText);
    }

    public static void main(String[] args) throws Exception {
        String hudong = encryptEcb("PJ-1712PJ-1712PJ-1712", "18621599608");
        System.out.println("加密后结果：" + hudong);
        String hudong1 = decryptEcb("shanghaihudong", hudong);

        System.out.println("解密后结果：" + hudong1);
    }
}
