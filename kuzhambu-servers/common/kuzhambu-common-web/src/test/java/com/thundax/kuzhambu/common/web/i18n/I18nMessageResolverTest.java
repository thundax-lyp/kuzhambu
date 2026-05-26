package com.thundax.kuzhambu.common.web.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

public class I18nMessageResolverTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final I18nMessageResolver resolver = new I18nMessageResolver(messageSource);

    @Test
    public void shouldResolveMessageByCurrentLocale() {
        Locale previous = LocaleContextHolder.getLocale();
        LocaleContextHolder.setLocale(Locale.US);
        messageSource.addMessage("auth.exception.invalid-captcha", Locale.US, "Invalid captcha");

        try {
            String message = resolver.resolve("auth.exception.invalid-captcha", "验证码错误");

            assertEquals("Invalid captcha", message);
        } finally {
            LocaleContextHolder.setLocale(previous);
        }
    }

    @Test
    public void shouldUseDefaultMessageWhenMessageKeyIsBlankOrMissing() {
        assertEquals("验证码错误", resolver.resolve(null, "验证码错误"));
        assertEquals("验证码错误", resolver.resolve("auth.exception.invalid-captcha", "验证码错误"));
    }
}
