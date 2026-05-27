package com.thundax.kuzhambu.system.interfaces.admin.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KuzhambuProperties.class, LoginProperties.class})
public class SystemAdminInterfaceConfiguration {}
