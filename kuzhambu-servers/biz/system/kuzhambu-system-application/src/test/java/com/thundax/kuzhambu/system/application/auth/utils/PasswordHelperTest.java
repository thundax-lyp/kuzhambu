package com.thundax.kuzhambu.system.application.auth.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PasswordHelperTest {

    @Test
    public void shouldValidateNoopPassword() {
        assertTrue(PasswordHelper.validate("admin", "{noop}admin"));
        assertFalse(PasswordHelper.validate("admin", "{noop}password"));
    }

    @Test
    public void shouldValidateEncryptedPassword() {
        String encryptedPassword = PasswordHelper.encrypt("admin");

        assertTrue(PasswordHelper.validate("admin", encryptedPassword));
        assertFalse(PasswordHelper.validate("password", encryptedPassword));
    }
}
