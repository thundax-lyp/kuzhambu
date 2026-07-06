package com.thundax.kuzhambu.operations.application.dashboard.service;

import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;

public interface OperationsDashboardApplicationService {

    OperationsDashboardOverviewResult overview(OperationsDashboardOverviewQuery query);
}
