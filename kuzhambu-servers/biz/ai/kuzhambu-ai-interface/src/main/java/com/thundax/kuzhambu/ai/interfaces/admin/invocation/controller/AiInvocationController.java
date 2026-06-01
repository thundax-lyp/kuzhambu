package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller;

import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler.AiInvocationInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
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

    public AiInvocationController(
            AiInvocationRepository invocationRepository, AiBatchJobApplicationService batchJobService) {
        this.invocationRepository = invocationRepository;
        this.batchJobService = batchJobService;
    }

    @Operation(summary = "获取AI调用记录", description = "ai:invocation:view")
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "调用读取")
    @PostMapping(value = "call/get")
    public AiInvocationResponses.CallRecordResponse getCallRecord(
            @Valid @RequestBody AiInvocationRequests.CallIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(invocationRepository.getCallRecord(request.getCallId()));
    }

    @Operation(summary = "获取AI候选", description = "ai:invocation:view")
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "候选读取")
    @PostMapping(value = "candidate/get")
    public AiInvocationResponses.CandidateResponse getCandidate(
            @Valid @RequestBody AiInvocationRequests.CandidateIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(invocationRepository.getCandidate(request.getCandidateId()));
    }

    @Operation(summary = "获取AI候选列表", description = "ai:invocation:view")
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "候选列表")
    @PostMapping(value = "candidate/list")
    public List<AiInvocationResponses.CandidateResponse> listCandidates(
            @Valid @RequestBody AiInvocationRequests.CandidateListRequest request) {
        return invocationRepository
                .listCandidates(
                        request.getContentType(), request.getContentId(), request.getCapability(), request.getStatus())
                .stream()
                .map(AiInvocationInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "拒绝AI候选", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "候选拒绝")
    @PostMapping(value = "candidate/reject")
    public AiInvocationResponses.CandidateResponse rejectCandidate(
            @Valid @RequestBody AiInvocationRequests.CandidateRejectRequest request) {
        AiCandidate candidate = invocationRepository.getCandidate(request.getCandidateId());
        assertCandidateFound(candidate, request.getCandidateId());
        candidate.reject(request.getErrorType(), request.getErrorMessage());
        invocationRepository.updateCandidate(candidate);
        return AiInvocationInterfaceAssembler.toResponse(invocationRepository.getCandidate(request.getCandidateId()));
    }

    @Operation(summary = "标记AI候选已应用", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "候选已应用")
    @PostMapping(value = "candidate/mark-applied")
    public AiInvocationResponses.CandidateResponse markCandidateApplied(
            @Valid @RequestBody AiInvocationRequests.CandidateIdRequest request) {
        AiCandidate candidate = invocationRepository.getCandidate(request.getCandidateId());
        assertCandidateFound(candidate, request.getCandidateId());
        candidate.markApplied(Instant.now());
        invocationRepository.updateCandidate(candidate);
        return AiInvocationInterfaceAssembler.toResponse(invocationRepository.getCandidate(request.getCandidateId()));
    }

    @Operation(summary = "获取AI批量任务", description = "ai:invocation:view")
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "批量读取")
    @PostMapping(value = "batch/get")
    public AiInvocationResponses.BatchJobResponse getBatch(
            @Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(batchJobService.get(request.getBatchId()));
    }

    @Operation(summary = "创建AI批量任务", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量创建")
    @PostMapping(value = "batch/create")
    public Long createBatch(@Valid @RequestBody AiInvocationRequests.BatchCreateRequest request) {
        return batchJobService.create(AiInvocationInterfaceAssembler.toCreateCommand(request));
    }

    @Operation(summary = "取消AI批量任务", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量取消")
    @PostMapping(value = "batch/cancel")
    public AiInvocationResponses.BatchJobResponse cancelBatch(
            @Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(batchJobService.cancel(request.getBatchId()));
    }

    @Operation(summary = "记录AI批量成功", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量成功")
    @PostMapping(value = "batch/record-success")
    public AiInvocationResponses.BatchJobResponse recordBatchSuccess(
            @Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(batchJobService.recordSuccess(request.getBatchId()));
    }

    @Operation(summary = "记录AI批量失败", description = "ai:invocation:edit")
    @HasPermission(value = "ai:invocation:edit")
    @SysLogger(value = "批量失败")
    @PostMapping(value = "batch/record-failure")
    public AiInvocationResponses.BatchJobResponse recordBatchFailure(
            @Valid @RequestBody AiInvocationRequests.BatchFailureRequest request) {
        return AiInvocationInterfaceAssembler.toResponse(
                batchJobService.recordFailure(request.getBatchId(), request.getFailureSummaryJson()));
    }

    @Operation(summary = "判断AI批量任务是否可继续派发", description = "ai:invocation:view")
    @HasPermission(value = "ai:invocation:view")
    @SysLogger(value = "批量派发判断")
    @PostMapping(value = "batch/can-dispatch")
    public Boolean canDispatchBatch(@Valid @RequestBody AiInvocationRequests.BatchIdRequest request) {
        return batchJobService.canDispatchNextUnit(request.getBatchId());
    }

    private void assertCandidateFound(AiCandidate candidate, Long candidateId) {
        if (candidate == null) {
            throw new BizException("AI candidate not found: " + candidateId);
        }
    }
}
