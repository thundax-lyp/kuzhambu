package com.thundax.kuzhambu.biz.auth.utils;

import com.thundax.kuzhambu.common.core.crypto.Sm3Digest;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

public final class PasswordHelper {

    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";

    private static final String ARG_SEPARATOR = ",";
    private static final int ARG_SALT = 0;
    private static final int ARG_SIZE = 2;

    private PasswordHelper() {}

    public static String encrypt(String plainPassword) {
        return encrypt(plainPassword, generateSalt());
    }

    public static boolean validate(String plainPassword, String encryptedPassword) {
        if (StringUtils.startsWithIgnoreCase(encryptedPassword, PREFIX)
                && StringUtils.endsWithIgnoreCase(encryptedPassword, SUFFIX)) {
            String queryString =
                    encryptedPassword.substring(PREFIX.length(), encryptedPassword.length() - SUFFIX.length());
            String[] args = StringUtils.split(queryString, ARG_SEPARATOR);
            if (args.length != ARG_SIZE) {
                return false;
            }

            return StringUtils.equals(encrypt(plainPassword, args[ARG_SALT]), encryptedPassword);
        }

        return StringUtils.equals(Sm3Digest.sm3Hex(plainPassword), encryptedPassword);
    }

    private static String encrypt(String plainPassword, String salt) {
        String encryptedPassword =
                Sm3Digest.sm3Hex((salt + ARG_SEPARATOR + plainPassword).getBytes(StandardCharsets.UTF_8));
        return PREFIX + salt + ARG_SEPARATOR + encryptedPassword + SUFFIX;
    }

    private static String generateSalt() {
        return String.format("%x", 0x1000 + new Random().nextInt(0xefff));
    }
}
