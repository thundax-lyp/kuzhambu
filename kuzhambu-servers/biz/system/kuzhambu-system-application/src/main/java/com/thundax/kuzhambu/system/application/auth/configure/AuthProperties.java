package com.thundax.kuzhambu.system.application.auth.configure;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.auth")
public class AuthProperties {

    private int maxLoginCount = 10;

    private int maxOnlineCount = 200;

    private int loginExpiredSeconds = 300;

    private String whiteCaptcha = StringUtils.EMPTY;

    private String validateCodeTemplate = StringUtils.EMPTY;
}
