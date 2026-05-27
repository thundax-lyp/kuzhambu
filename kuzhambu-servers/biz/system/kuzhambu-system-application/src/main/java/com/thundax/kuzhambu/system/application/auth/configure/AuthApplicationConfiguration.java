package com.thundax.kuzhambu.system.application.auth.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AuthProperties.class, CaptchaWhitelistProperties.class})
public class AuthApplicationConfiguration {}
