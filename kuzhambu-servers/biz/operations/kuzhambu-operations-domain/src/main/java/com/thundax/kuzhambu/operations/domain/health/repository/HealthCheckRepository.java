package com.thundax.kuzhambu.operations.domain.health.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import java.util.List;

public interface HealthCheckRepository {

    HealthCheckRecord getById(HealthCheckId id);

    List<HealthCheckRecord> listLatestByComponent();

    PageResult<HealthCheckRecord> page(String component, String healthStatus, int pageNo, int pageSize);

    HealthCheckId insert(HealthCheckRecord record);

    int update(HealthCheckRecord record);

    int deleteById(HealthCheckId id);
}
