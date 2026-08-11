package com.thundax.kuzhambu.operations.domain.health.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import java.util.List;

public interface HealthAlertRepository {

    HealthAlertRecord getById(HealthAlertId id);

    HealthAlertRecord getBySource(String sourceRefType, Long sourceRefId, String alertType);

    PageResult<HealthAlertRecord> page(
            String component,
            String alertLevel,
            String alertStatus,
            String sourceRefType,
            Long sourceRefId,
            Long latestCheckId,
            int pageNo,
            int pageSize);

    List<HealthAlertRecord> listOpenByComponent(String component);

    List<HealthAlertRecord> listOpenSummary();

    HealthAlertId insert(HealthAlertRecord record);

    int update(HealthAlertRecord record);
}
