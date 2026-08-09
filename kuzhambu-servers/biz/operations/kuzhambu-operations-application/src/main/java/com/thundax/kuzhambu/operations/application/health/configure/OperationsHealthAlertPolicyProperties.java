package com.thundax.kuzhambu.operations.application.health.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.health.alert")
public class OperationsHealthAlertPolicyProperties {

    private int degradedThreshold = 3;
    private int recoveryUpThreshold = 2;
    private int staleMinutes = 10;
    private int writeBlockStaleMinutes = 30;
}
