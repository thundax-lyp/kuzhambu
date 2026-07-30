package com.thundax.kuzhambu.operations.application.dashboard.support;

import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import java.time.Instant;

public interface OperationsDashboardSummaryGateway {

    OperationsCrossDomainSummary loadSummary(
            Instant periodStart,
            Instant periodEnd,
            String bucketType,
            OperationsDashboardPermissionSnapshot permissions);
}
