package com.thundax.kuzhambu.ai.infra.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class WorkerAiSignatureSupport {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    public String sign(
            String method, String path, String timestamp, String requestId, String requestBody, String secret) {
        if (isBlank(secret)) {
            throw new IllegalStateException("Worker internal secret is not configured");
        }
        String signingInput = signingInput(method, path, timestamp, requestId, requestBody);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return hex(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign worker request", ex);
        }
    }

    public String signingInput(String method, String path, String timestamp, String requestId, String requestBody) {
        return method + "\n" + path + "\n" + timestamp + "\n" + requestId + "\n" + sha256(requestBody);
    }

    public String sha256(String requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return hex(digest.digest(nullToEmpty(requestBody).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String hex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 0xFF;
            chars[index * 2] = HEX[value >>> 4];
            chars[index * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(chars);
    }
}
