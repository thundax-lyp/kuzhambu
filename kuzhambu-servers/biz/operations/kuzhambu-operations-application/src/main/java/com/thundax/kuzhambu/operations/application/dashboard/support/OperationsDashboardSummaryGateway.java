package com.thundax.kuzhambu.operations.application.dashboard.support;

import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import java.util.Date;

public interface OperationsDashboardSummaryGateway {

    OperationsCrossDomainSummary loadSummary(Date periodStart, Date periodEnd, String bucketType);
}
