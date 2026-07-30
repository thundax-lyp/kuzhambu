package com.thundax.kuzhambu.operations.domain.health.model.entity;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthAlertRecord {

    private HealthAlertId id;
    private String component;
    private String alertType;
    private String alertLevel;
    private String alertStatus;
    private String sourceRefType;
    private Long sourceRefId;
    private HealthCheckId latestCheckId;
    private String message;
    private String suggestion;
    private String recoveryAction;
    private String recoveryTarget;
    private Instant firstTriggeredAt;
    private Instant lastTriggeredAt;
    private Instant ackedAt;
    private Long ackedByUserId;
    private Instant recoveredAt;
    private String failureReason;
}
