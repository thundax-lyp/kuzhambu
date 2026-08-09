package com.thundax.kuzhambu.operations.application.task.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskQuery;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskDetailResult;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskPageResult;

public interface TaskApplicationService {

    PageResult<OperationsTaskPageResult> page(OperationsTaskQuery query, PageQuery pageQuery);

    OperationsTaskDetailResult detail(OperationsTaskDetailQuery query);
}
