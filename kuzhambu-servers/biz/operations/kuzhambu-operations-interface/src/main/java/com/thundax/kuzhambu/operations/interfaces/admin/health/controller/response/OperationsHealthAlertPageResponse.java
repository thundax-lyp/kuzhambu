package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthAlertPageResponse {
    private Long alertId;
    private String component;
    private String alertType;
    private String alertLevel;
    private String alertStatus;
    private String sourceRefType;
    private Long sourceRefId;
    private Long latestCheckId;
    private String message;
    private String suggestion;
    private String recoveryAction;
    private String recoveryTarget;
    private Date firstTriggeredAt;
    private Date lastTriggeredAt;
    private Date ackedAt;
    private Long ackedByUserId;
    private Date recoveredAt;
    private String failureReason;
}
