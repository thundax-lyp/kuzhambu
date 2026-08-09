package com.thundax.kuzhambu.system.application.auth.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.system.application.auth.configure.CaptchaWhitelistProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class CaptchaWhitelistPolicyTest {

    @Test
    void shouldMatchTrimmedNonBlankConfiguredValuesWhenEnabled() {
        CaptchaWhitelistProperties properties = new CaptchaWhitelistProperties();
        properties.setWhitelistEnabled(true);
        properties.setWhitelistValues(List.of("  1234  ", "", "1234", "5678"));

        CaptchaWhitelistPolicy policy = CaptchaWhitelistPolicy.from(properties);

        assertThat(policy.matches(" 1234 ")).isTrue();
        assertThat(policy.matches("5678")).isTrue();
        assertThat(policy.matches(" ")).isFalse();
        assertThat(policy.matches("9999")).isFalse();
    }

    @Test
    void shouldNotMatchWhenDisabledOrPropertiesAreMissing() {
        CaptchaWhitelistProperties properties = new CaptchaWhitelistProperties();
        properties.setWhitelistValues(List.of("1234"));

        assertThat(CaptchaWhitelistPolicy.from(properties).matches("1234")).isFalse();
        assertThat(CaptchaWhitelistPolicy.from(null).matches("1234")).isFalse();
    }
}
