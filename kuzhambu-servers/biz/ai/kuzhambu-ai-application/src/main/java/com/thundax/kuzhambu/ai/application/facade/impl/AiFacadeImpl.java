package com.thundax.kuzhambu.ai.application.facade.impl;

import com.thundax.kuzhambu.ai.application.facade.assembler.AiFacadeAssembler;
import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.AiReportSummaryQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.CanDispatchNextAiBatchUnitQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.DiscoveryAiStreamHandler;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiInvocationLogFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiFacadeImpl implements AiFacade {

    private final AiReportApplicationService aiReportApplicationService;
    private final AiBatchJobApplicationService aiBatchJobApplicationService;
    private final DiscoveryAiApplicationService discoveryAiApplicationService;
    private final KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService;
    private final AiCandidateApplicationService aiCandidateApplicationService;
    private final AiInvocationRepository aiInvocationRepository;
    private final AiFacadeAssembler aiFacadeAssembler;

    public AiFacadeImpl(
            AiReportApplicationService aiReportApplicationService,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            DiscoveryAiApplicationService discoveryAiApplicationService,
            KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService,
            AiCandidateApplicationService aiCandidateApplicationService,
            AiInvocationRepository aiInvocationRepository,
            AiFacadeAssembler aiFacadeAssembler) {
        this.aiReportApplicationService = aiReportApplicationService;
        this.aiBatchJobApplicationService = aiBatchJobApplicationService;
        this.discoveryAiApplicationService = discoveryAiApplicationService;
        this.knowledgeAiExtractionApplicationService = knowledgeAiExtractionApplicationService;
        this.aiCandidateApplicationService = aiCandidateApplicationService;
        this.aiInvocationRepository = aiInvocationRepository;
        this.aiFacadeAssembler = aiFacadeAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(aiReportApplicationService.summary(new AiReportSummaryQuery(
                request.getPeriodStart() == null
                        ? null
                        : request.getPeriodStart().toInstant(),
                request.getPeriodEnd() == null ? null : request.getPeriodEnd().toInstant(),
                request.getBucketType() == null ? null : AiReportBucketType.from(request.getBucketType()))));
    }

    @Override
    public DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                discoveryAiApplicationService.understandQuery(aiFacadeAssembler.toDiscoveryAiCommand(request)));
    }

    @Override
    public DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                discoveryAiApplicationService.generateAnswer(aiFacadeAssembler.toDiscoveryAiCommand(request)));
    }

    @Override
    public DiscoveryAiFacadeResponse streamDiscoveryAnswer(
            DiscoveryAiFacadeRequest request, DiscoveryAiStreamHandler streamHandler) {
        return aiFacadeAssembler.toFacadeResponse(
                discoveryAiApplicationService.streamAnswer(aiFacadeAssembler.toDiscoveryAiCommand(request), event -> {
                    if (event != null && StringUtils.hasText(event.getDeltaText()) && streamHandler != null) {
                        streamHandler.onDelta(event.getDeltaText());
                    }
                }));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(knowledgeAiExtractionApplicationService.extractRelations(
                aiFacadeAssembler.toKnowledgeAiExtractionCommand(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(knowledgeAiExtractionApplicationService.extractGraph(
                aiFacadeAssembler.toKnowledgeAiExtractionCommand(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(knowledgeAiExtractionApplicationService.extractLineage(
                aiFacadeAssembler.toKnowledgeAiExtractionCommand(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(knowledgeAiExtractionApplicationService.extractTags(
                aiFacadeAssembler.toKnowledgeAiExtractionCommand(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public AiBatchJobFacadeResponse getBatchJob(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.get(new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(batchId))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request) {
        if (request == null) {
            return null;
        }
        Long batchId = AiBatchJobIdCodec.toValue(aiBatchJobApplicationService.create(new AiBatchJobCreateCommand(
                request.getScope(),
                AiBusinessCapability.fromAlias(request.getCapability()),
                AiContentRef.ofNullable(request.getContentType(), null),
                request.getTotalCount(),
                request.getFailureSummaryJson())));
        return aiFacadeAssembler.toActionResponse(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDispatchNextBatchUnit(Long batchId) {
        return aiBatchJobApplicationService.canDispatchNextUnit(
                new CanDispatchNextAiBatchUnitQuery(AiBatchJobIdCodec.toDomain(batchId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchSuccess(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(aiBatchJobApplicationService.recordSuccess(
                new RecordAiBatchJobCommand(AiBatchJobIdCodec.toDomain(batchId))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.recordFailure(new RecordAiBatchJobFailureCommand(
                        AiBatchJobIdCodec.toDomain(request.getBatchId()), request.getFailureSummaryJson())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse cancelBatchJob(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.cancel(new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(batchId))));
    }

    @Override
    @Transactional(readOnly = true)
    public AiInvocationLogFacadeDto getInvocationLog(GetAiInvocationLogFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(
                aiInvocationRepository.getInvocationLog(AiCallIdCodec.toDomain(request.getCallId())));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(
                aiInvocationRepository.getCandidate(AiCandidateIdCodec.toDomain(request.getCandidateId())));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(
                aiCandidateApplicationService.requirePendingForApply(new RequireAiCandidateForApplyQuery(
                        AiCandidateIdCodec.toDomain(request.getCandidateId()),
                        AiContentRef.ofNullable(request.getContentType(), request.getContentId()),
                        AiBusinessCapability.fromAlias(request.getCapability()),
                        AiTargetObjectIdCodec.toDomain(request.getObjectId()))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiCandidateApplicationService.markApplied(new ApplyAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getResultFormat(),
                request.getResultPayload(),
                request.getAppliedAt())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateFacadeDto rejectCandidate(RejectAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiCandidateApplicationService.reject(new RejectAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getErrorType(),
                request.getErrorMessage())));
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("AiFacade bridge not implemented yet");
    }
}
