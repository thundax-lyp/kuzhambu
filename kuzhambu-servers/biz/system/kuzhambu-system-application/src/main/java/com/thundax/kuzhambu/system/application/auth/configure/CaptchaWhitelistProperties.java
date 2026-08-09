package com.thundax.kuzhambu.system.application.auth.configure;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.auth.captcha")
public class CaptchaWhitelistProperties {

    private boolean whitelistEnabled;
    private Collection<String> whitelistValues = Collections.emptyList();
}
