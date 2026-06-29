package com.thundax.kuzhambu.operations.application.health.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import java.util.List;

public interface HealthCheckApplicationService {

    List<OperationsHealthSummaryResult> summary();

    PageResult<OperationsHealthPageResult> page(OperationsHealthPageQuery query, PageQuery pageQuery);
}
