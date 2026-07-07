package com.thundax.kuzhambu.operations.domain.health.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import java.util.Date;
import java.util.List;

public interface HealthCheckRepository {

    HealthCheckRecord getById(HealthCheckId id);

    List<HealthCheckRecord> listLatestByComponent();

    PageResult<HealthCheckRecord> page(
            String component,
            String healthStatus,
            String probeSource,
            String probeTarget,
            Date checkedAtStart,
            Date checkedAtEnd,
            int pageNo,
            int pageSize);

    List<HealthTrendBucket> listTrend(
            String component, String probeSource, Date periodStart, Date periodEnd, String bucketType);

    HealthCheckId insert(HealthCheckRecord record);

    int update(HealthCheckRecord record);

    int deleteById(HealthCheckId id);
}
