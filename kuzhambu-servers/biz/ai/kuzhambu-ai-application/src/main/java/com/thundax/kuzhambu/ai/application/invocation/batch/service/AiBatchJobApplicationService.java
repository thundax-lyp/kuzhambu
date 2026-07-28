package com.thundax.kuzhambu.ai.application.invocation.batch.service;

import com.thundax.kuzhambu.ai.application.invocation.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;

public interface AiBatchJobApplicationService {

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批次读取 AI 批任务状态的跨模块入口")
    AiBatchJobResult get(Long batchId);

    @LayerPublicApi(reason = "AI 批任务后台查询入口")
    PageResult<AiBatchJobResult> page(
            String scope, String capability, String status, String contentType, Long contentId, PageQuery pageQuery);

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批量选择范围创建 AI 批任务的跨模块入口")
    Long create(AiBatchJobCreateCommand command);

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批次派发子任务前校验批任务状态的跨模块入口")
    boolean canDispatchNextUnit(Long batchId);

    @LayerPublicApi(reason = "Knowledge 图谱抽取单个子任务成功后回写 AI 批任务计数的跨模块入口")
    AiBatchJobResult recordSuccess(Long batchId);

    @LayerPublicApi(reason = "Knowledge 图谱抽取单个子任务失败后回写 AI 批任务计数的跨模块入口")
    AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson);

    @LayerPublicApi(reason = "Knowledge 图谱抽取取消批任务时停止未开始单元的跨模块入口")
    AiBatchJobResult cancel(Long batchId);
}
