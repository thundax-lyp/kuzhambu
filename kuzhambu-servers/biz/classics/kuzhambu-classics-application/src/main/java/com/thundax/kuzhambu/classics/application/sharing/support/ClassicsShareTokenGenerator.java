package com.thundax.kuzhambu.classics.application.sharing.support;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ClassicsShareTokenGenerator {
    private static final int TOKEN_BYTES = 24;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toLowerCase(Locale.ROOT);
    }
}
