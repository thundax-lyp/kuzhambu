package com.thundax.kuzhambu.operations.application.restore.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;

public interface RestoreApplicationService {

    OperationsRestoreExecuteResult execute(OperationsRestoreExecuteCommand command);

    PageResult<OperationsRestorePageResult> page(OperationsRestorePageQuery query, PageQuery pageQuery);

    OperationsRestoreDetailResult detail(OperationsRestoreDetailQuery query);
}
