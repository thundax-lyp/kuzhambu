package com.thundax.kuzhambu.operations.application.health.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class OperationsHealthAlertPolicyProperties {

    @Value("${kuzhambu.operations.health.alert.degraded-threshold:3}")
    private int degradedThreshold;

    @Value("${kuzhambu.operations.health.alert.recovery-up-threshold:2}")
    private int recoveryUpThreshold;

    @Value("${kuzhambu.operations.health.alert.stale-minutes:10}")
    private int staleMinutes;

    @Value("${kuzhambu.operations.health.alert.write-block-stale-minutes:30}")
    private int writeBlockStaleMinutes;
}
