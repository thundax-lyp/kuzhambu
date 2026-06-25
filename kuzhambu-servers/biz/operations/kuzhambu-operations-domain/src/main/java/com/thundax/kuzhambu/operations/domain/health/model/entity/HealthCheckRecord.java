package com.thundax.kuzhambu.operations.domain.health.model.entity;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckRecord {

    private HealthCheckId id;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private Date checkedAt;
}
