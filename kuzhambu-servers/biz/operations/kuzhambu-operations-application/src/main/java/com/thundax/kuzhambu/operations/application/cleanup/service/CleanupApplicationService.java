package com.thundax.kuzhambu.operations.application.cleanup.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;

public interface CleanupApplicationService {

    OperationsCleanupDetailResult execute(OperationsCleanupExecuteCommand command);

    OperationsCleanupDetailResult executeScheduled(OperationsCleanupExecuteCommand command);

    PageResult<OperationsCleanupPageResult> page(OperationsCleanupQuery query, PageQuery pageQuery);

    OperationsCleanupDetailResult detail(OperationsCleanupDetailQuery query);
}
