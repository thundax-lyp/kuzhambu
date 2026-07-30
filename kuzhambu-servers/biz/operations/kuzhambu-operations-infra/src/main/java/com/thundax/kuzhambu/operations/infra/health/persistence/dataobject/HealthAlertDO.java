package com.thundax.kuzhambu.operations.infra.health.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("operations_health_alert")
public class HealthAlertDO {

    @TableId(type = IdType.AUTO)
    private Long id;

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
    private Instant firstTriggeredAt;
    private Instant lastTriggeredAt;
    private Instant ackedAt;
    private Long ackedByUserId;
    private Instant recoveredAt;
    private String failureReason;
}
