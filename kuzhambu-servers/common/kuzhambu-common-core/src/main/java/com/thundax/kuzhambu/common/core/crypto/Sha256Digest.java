package com.thundax.kuzhambu.common.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;

public final class Sha256Digest {

    private static final String ALGORITHM_SHA_256 = "SHA-256";

    private Sha256Digest() {}

    public static String hashBase64Url(String input) {
        if (input == null) {
            return StringUtils.EMPTY;
        }
        return hashBase64Url(input.getBytes(StandardCharsets.US_ASCII));
    }

    public static String hashBase64Url(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_SHA_256);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(input));

        } catch (NoSuchAlgorithmException e) {
            return StringUtils.EMPTY;
        }
    }
}
