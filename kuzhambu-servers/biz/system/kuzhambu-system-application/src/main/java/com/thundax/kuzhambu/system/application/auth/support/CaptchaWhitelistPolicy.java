package com.thundax.kuzhambu.system.application.auth.support;

import com.thundax.kuzhambu.system.application.auth.configure.CaptchaWhitelistProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class CaptchaWhitelistPolicy {

    private final boolean enabled;
    private final Set<String> values;

    private CaptchaWhitelistPolicy(boolean enabled, Collection<String> values) {
        this.enabled = enabled;
        this.values = normalize(values);
    }

    public static CaptchaWhitelistPolicy from(CaptchaWhitelistProperties properties) {
        if (properties == null) {
            return disabled();
        }
        return new CaptchaWhitelistPolicy(properties.isWhitelistEnabled(), properties.getWhitelistValues());
    }

    public static CaptchaWhitelistPolicy disabled() {
        return new CaptchaWhitelistPolicy(false, Collections.emptyList());
    }

    public boolean matches(String captcha) {
        return enabled && values.contains(StringUtils.trimToEmpty(captcha));
    }

    private static Set<String> normalize(Collection<String> whitelistValues) {
        Set<String> normalizedValues = new LinkedHashSet<>();
        if (whitelistValues == null) {
            return normalizedValues;
        }
        for (String whitelistValue : whitelistValues) {
            String value = StringUtils.trimToEmpty(whitelistValue);
            if (StringUtils.isNotBlank(value)) {
                normalizedValues.add(value);
            }
        }
        return normalizedValues;
    }
}
