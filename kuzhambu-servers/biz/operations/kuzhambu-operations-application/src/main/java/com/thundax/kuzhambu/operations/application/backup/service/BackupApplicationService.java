package com.thundax.kuzhambu.operations.application.backup.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupPageQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;

public interface BackupApplicationService {

    OperationsBackupExecuteResult execute(OperationsBackupExecuteCommand command);

    PageResult<OperationsBackupPageResult> page(OperationsBackupPageQuery query, PageQuery pageQuery);

    OperationsBackupDetailResult detail(OperationsBackupDetailQuery query);
}
