package com.thundax.kuzhambu.operations.domain.health.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import java.time.Instant;
import java.util.List;

public interface HealthCheckRepository {

    HealthCheckRecord getById(HealthCheckId id);

    List<HealthCheckRecord> listLatestByComponent();

    PageResult<HealthCheckRecord> page(
            String component,
            String healthStatus,
            String probeSource,
            String probeTarget,
            Instant checkedAtStart,
            Instant checkedAtEnd,
            int pageNo,
            int pageSize);

    List<HealthTrendBucket> listTrend(
            String component, String probeSource, Instant periodStart, Instant periodEnd, String bucketType);

    HealthCheckId insert(HealthCheckRecord record);

    int update(HealthCheckRecord record);

    int deleteById(HealthCheckId id);

    default List<HealthCheckId> listExpiredCheckIds(Instant checkedBefore, int limit) {
        return List.of();
    }
}
