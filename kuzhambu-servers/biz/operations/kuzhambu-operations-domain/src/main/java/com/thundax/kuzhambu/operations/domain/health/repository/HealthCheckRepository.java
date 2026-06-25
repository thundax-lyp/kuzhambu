package com.thundax.kuzhambu.operations.domain.health.repository;

import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;

public interface HealthCheckRepository {

    HealthCheckRecord getById(HealthCheckId id);

    HealthCheckId insert(HealthCheckRecord record);

    int update(HealthCheckRecord record);

    int deleteById(HealthCheckId id);
}
