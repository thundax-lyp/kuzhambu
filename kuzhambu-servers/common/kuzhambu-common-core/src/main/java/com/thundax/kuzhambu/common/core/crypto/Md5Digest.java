package com.thundax.kuzhambu.common.core.crypto;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public final class Md5Digest {

    private static final String ALGORITHM_MD5 = "MD5";

    private Md5Digest() {}

    public static String encrypt(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_MD5);

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, readBytes);
            }
            return Hex.encodeHexString(digest.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            return StringUtils.EMPTY;
        }
    }

    public static String encrypt(String input) {
        return encrypt(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String encrypt(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_MD5);
            return Hex.encodeHexString(digest.digest(input));

        } catch (NoSuchAlgorithmException e) {
            return StringUtils.EMPTY;
        }
    }

    public static String encrypt16(String input) {
        return encrypt(input.getBytes(StandardCharsets.UTF_8)).substring(8, 24);
    }
}
