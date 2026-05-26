package com.thundax.kuzhambu.common.web.i18n;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

public class I18nMessageResolver {

    private final MessageSource messageSource;

    public I18nMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String resolve(String messageKey, String defaultMessage, Object... args) {
        if (!StringUtils.hasText(messageKey)) {
            return defaultMessage;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageKey, args, defaultMessage, locale);
    }
}
