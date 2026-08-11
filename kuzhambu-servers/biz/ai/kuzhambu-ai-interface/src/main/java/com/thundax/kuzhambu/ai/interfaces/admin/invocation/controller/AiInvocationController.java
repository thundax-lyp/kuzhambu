package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller;

import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler.AiInvocationInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.BatchJobResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.CandidateResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.IdResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.InvocationLogResponse;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.InvocationSummaryResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-调用", description = "AI调用记录、候选和批量任务")
@SysLogger(module = {"AI", "调用"})
@RequestMapping(value = "/api/ai/invocation")
@WrappedApiController
public class AiInvocationController {

    private final AiInvocationRepository invocationRepository;
    private final AiBatchJobApplicationService batchJobService;
    private final AiCandidateApplicationService aiCandidateApplicationService;

    public AiInvocationController(
            AiInvocationRepository invocationRepository,
            AiBatchJobApplicationService batchJobService,
            AiCandidateApplicationService aiCandidateApplicationService) {
        this.invocationRepository = invocationRepository;
        this.batchJobService = batchJobService;
        this.aiCandidateApplicationService = aiCandidateApplicationService;
    }

    @Operation(summary = "获取AI调用记录", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "调用读取")
    @PostMapping(value = "invocation-log/get")
    public InvocationLogResponse getInvocationLog(@Valid @RequestBody AiInvocationRequests.CallIdRequest request) {
        return toInvocationLogResponse(invocationRepository.getByCallId(AiCallIdCodec.toDomain(request.getCallId())));
    }

    @Operation(summary = "分页查询AI调用记录", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "调用分页")
    @PostMapping(value = "invocation-log/page")
    public PageResponse<InvocationLogResponse> pageInvocationLogs(
            @Valid @RequestBody AiInvocationRequests.InvocationLogPageRequest request) {
        PageQuery pageQuery = PageInterfaceAssembler.toPageQuery(request);
        return PageResponseHelper.fromPageResult(
                invocationRepository.pageByFilter(
                        request.getScope(),
                        toCapability(request.getCapability()),
                        AiContentRefCodec.toDomain(request.getContentType(), request.getContentId()),
                        toInvocationStatus(request.getStatus()),
                        request.getServiceRole(),
                        AiModelNameCodec.toDomain(request.getModelName()),
                        request.getFallbackUsed(),
                        request.getRequestedAtStart(),
                        request.getRequestedAtEnd(),
                        pageQuery.getPageNo(),
                        pageQuery.getPageSize()),
                AiInvocationInterfaceAssembler::toResponse);
    }

    @Operation(summary = "统计AI调用记录", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "调用统计")
    @PostMapping(value = "invocation-log/summary/get")
    public InvocationSummaryResponse getInvocationLogSummary(
            @Valid @RequestBody AiInvocationRequests.InvocationSummaryRequest request) {
        return AiInvocationInterfaceAssembler.toSummaryResponse(
                request,
                invocationRepository.listInvocationLogs(
                        request.getScope(),
                        toCapability(request.getCapability()),
                        request.getServiceRole(),
                        request.getPeriodStart(),
                        request.getPeriodEnd()));
    }

    @Operation(summary = "获取AI候选", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "候选读取")
    @PostMapping(value = "candidate/get")
    public CandidateResponse getCandidate(@Valid @RequestBody AiInvocationRequests.CandidateIdRequest request) {
        return toCandidateResponse(
                invocationRepository.getByCandidateId(AiCandidateIdCodec.toDomain(request.getCandidateId())));
    }

    @Operation(summary = "获取AI候选列表", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "候选列表")
    @PostMapping(value = "candidate/list")
    public List<CandidateResponse> listCandidates(
            @Valid @RequestBody AiInvocationRequests.CandidateListRequest request) {
        return invocationRepository
                .listCandidates(
                        AiContentRefCodec.toDomain(request.getContentType(), request.getContentId()),
                        AiTargetObjectIdCodec.toDomain(request.getObjectId()),
                        toCapability(request.getCapability()),
                        toCandidateStatus(request.getStatus()))
                .stream()
                .map(AiInvocationInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "拒绝AI候选", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "候选拒绝")
    @PostMapping(value = "candidate/reject")
    public CandidateResponse rejectCandidate(@Valid @RequestBody AiInvocationRequests.CandidateRejectRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                aiCandidateApplicationService.reject(AiInvocationInterfaceAssembler.toRejectCommand(request)));
    }

    @Operation(summary = "标记AI候选已应用", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "候选已应用")
    @PostMapping(value = "candidate/apply")
    public CandidateResponse applyCandidate(
            @Valid @RequestBody AiInvocationRequests.CandidateMarkAppliedRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(aiCandidateApplicationService.markApplied(
                AiInvocationInterfaceAssembler.toMarkAppliedCommand(request)));
    }

    private AiBusinessCapability toCapability(String value) {
        return isBlank(value) ? null : AiBusinessCapability.from(value);
    }

    private AiInvocationStatus toInvocationStatus(String value) {
        return isBlank(value) ? null : AiInvocationStatus.from(value);
    }

    private AiCandidateStatus toCandidateStatus(String value) {
        return isBlank(value) ? null : AiCandidateStatus.from(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Operation(summary = "获取AI批量任务", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "批量读取")
    @PostMapping(value = "batch/get")
    public BatchJobResponse getBatch(@Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                batchJobService.get(AiInvocationInterfaceAssembler.toGetBatchQuery(request)));
    }

    @Operation(summary = "创建AI批量任务", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量创建")
    @PostMapping(value = "batch/create")
    public IdResponse createBatch(@Valid @RequestBody AiInvocationRequests.BatchCreateRequest request) {
        return IdResponse.builder()
                .id(AiBatchJobIdCodec.toValue(
                        batchJobService.create(AiInvocationInterfaceAssembler.toCreateCommand(request))))
                .build();
    }

    @Operation(summary = "取消AI批量任务", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量取消")
    @PostMapping(value = "batch/cancel")
    public BatchJobResponse cancelBatch(@Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                batchJobService.cancel(AiInvocationInterfaceAssembler.toCancelBatchCommand(request)));
    }

    @Operation(summary = "记录AI批量成功", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量成功")
    @PostMapping(value = "batch/success/update")
    public BatchJobResponse updateBatchSuccess(@Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                batchJobService.recordSuccess(AiInvocationInterfaceAssembler.toRecordBatchSuccessCommand(request)));
    }

    @Operation(summary = "记录AI批量失败", description = "ai:invocation:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量失败")
    @PostMapping(value = "batch/failure/update")
    public BatchJobResponse updateBatchFailure(@Valid @RequestBody AiInvocationRequests.BatchFailureRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                batchJobService.recordFailure(AiInvocationInterfaceAssembler.toRecordBatchFailureCommand(request)));
    }

    @Operation(summary = "判断AI批量任务是否可继续派发", description = "ai:invocation:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "批量派发判断")
    @PostMapping(value = "batch/dispatch/get")
    public Boolean getBatchDispatchable(@Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return batchJobService.canDispatchNextUnit(AiInvocationInterfaceAssembler.toCanDispatchBatchQuery(request));
    }

    private static InvocationLogResponse toInvocationLogResponse(AiInvocationLog invocationLog) {
        return invocationLog == null
                ? InvocationLogResponse.builder().build()
                : AiInvocationInterfaceAssembler.toResponse(invocationLog);
    }

    private static CandidateResponse toCandidateResponse(AiCandidate candidate) {
        return candidate == null
                ? CandidateResponse.builder().build()
                : AiInvocationInterfaceAssembler.toResponse(candidate);
    }
}
