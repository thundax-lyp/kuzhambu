package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;

public interface AiBatchJobApplicationService {

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批次读取 AI 批任务状态的跨模块入口")
    AiBatchJobResult get(AiBatchJobId batchId);

    @LayerPublicApi(reason = "AI 批任务后台查询入口")
    PageResult<AiBatchJobResult> page(
            String scope,
            AiBusinessCapability capability,
            AiBatchJobStatus status,
            AiContentRef contentRef,
            PageQuery pageQuery);

    @LayerPublicApi(reason = "AI 精修任务按能力集合查询自有批任务入口")
    PageResult<AiBatchJobResult> pageByCapabilities(
            String scope,
            List<AiBusinessCapability> capabilities,
            AiBatchJobStatus status,
            AiContentRef contentRef,
            PageQuery pageQuery);

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批量选择范围创建 AI 批任务的跨模块入口")
    AiBatchJobId create(AiBatchJobCreateCommand command);

    @LayerPublicApi(reason = "Knowledge 图谱抽取按批次派发子任务前校验批任务状态的跨模块入口")
    boolean canDispatchNextUnit(AiBatchJobId batchId);

    @LayerPublicApi(reason = "Knowledge 图谱抽取单个子任务成功后回写 AI 批任务计数的跨模块入口")
    AiBatchJobResult recordSuccess(AiBatchJobId batchId);

    @LayerPublicApi(reason = "AI 精修任务完成时按运行中状态条件回写批任务，避免取消竞态覆盖终态")
    AiBatchJobResult recordSuccessIfRunning(AiBatchJobId batchId);

    @LayerPublicApi(reason = "Knowledge 图谱抽取单个子任务失败后回写 AI 批任务计数的跨模块入口")
    AiBatchJobResult recordFailure(AiBatchJobId batchId, String failureSummaryJson);

    @LayerPublicApi(reason = "AI 精修任务失败时按运行中状态条件回写批任务，避免取消竞态覆盖终态")
    AiBatchJobResult recordFailureIfRunning(AiBatchJobId batchId, String failureSummaryJson);

    @LayerPublicApi(reason = "AI 精修任务部分完成时保留 PARTIAL 终态")
    AiBatchJobResult recordPartialIfRunning(AiBatchJobId batchId, String failureSummaryJson);

    @LayerPublicApi(reason = "AI 精修任务恢复清理运行中过期批任务")
    int expireRunning(
            String scope,
            List<AiBusinessCapability> capabilities,
            Instant requestedBefore,
            String failureSummaryJson,
            int limit);

    @LayerPublicApi(reason = "Knowledge 图谱抽取取消批任务时停止未开始单元的跨模块入口")
    AiBatchJobResult cancel(AiBatchJobId batchId);
}
