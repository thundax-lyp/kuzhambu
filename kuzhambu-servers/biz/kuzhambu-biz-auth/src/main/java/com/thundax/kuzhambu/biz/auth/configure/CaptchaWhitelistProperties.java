package com.thundax.kuzhambu.biz.auth.configure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.auth.captcha")
public class CaptchaWhitelistProperties {

    private boolean whitelistEnabled;
    private Collection<String> whitelistValues = Collections.emptyList();

    public CaptchaWhitelistProperties() {}

    private CaptchaWhitelistProperties(boolean enabled, Collection<String> whitelistValues) {
        this.whitelistEnabled = enabled;
        this.whitelistValues = whitelistValues;
    }

    public static CaptchaWhitelistProperties of(boolean enabled, Collection<String> whitelistValues) {
        return new CaptchaWhitelistProperties(enabled, whitelistValues);
    }

    public static CaptchaWhitelistProperties disabled() {
        return new CaptchaWhitelistProperties(false, Collections.emptyList());
    }

    public boolean matches(String captcha) {
        return whitelistEnabled && normalize(whitelistValues).contains(StringUtils.trimToEmpty(captcha));
    }

    public Set<String> getWhitelistValues() {
        return Collections.unmodifiableSet(normalize(whitelistValues));
    }

    public void setWhitelistValues(String whitelistValues) {
        String[] splitValues = StringUtils.split(whitelistValues, ',');
        this.whitelistValues = splitValues == null ? Collections.emptyList() : Arrays.asList(splitValues);
    }

    private static Set<String> normalize(Collection<String> whitelistValues) {
        Set<String> values = new LinkedHashSet<>();
        if (whitelistValues == null) {
            return values;
        }
        for (String whitelistValue : whitelistValues) {
            String value = StringUtils.trimToEmpty(whitelistValue);
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values;
    }
}
