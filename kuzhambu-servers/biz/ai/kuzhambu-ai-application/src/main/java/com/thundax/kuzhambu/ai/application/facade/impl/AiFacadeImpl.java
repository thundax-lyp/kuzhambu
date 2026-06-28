package com.thundax.kuzhambu.ai.application.facade.impl;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.discovery.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.facade.assembler.AiFacadeAssembler;
import com.thundax.kuzhambu.ai.application.knowledge.service.impl.KnowledgeAiExtractionApplicationServiceImpl;
import com.thundax.kuzhambu.ai.application.report.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.domain.knowledge.service.KnowledgeAiExtractionDomainService;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCallRecordFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCallRecordFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiFacadeImpl implements AiFacade {

    private final AiReportApplicationService aiReportApplicationService;
    private final AiBatchJobApplicationService aiBatchJobApplicationService;
    private final DiscoveryAiApplicationService discoveryAiApplicationService;
    private final KnowledgeAiExtractionDomainService knowledgeAiExtractionDomainService;
    private final AiInvocationRepository aiInvocationRepository;
    private final AiCandidateDomainService aiCandidateDomainService;
    private final AiFacadeAssembler aiFacadeAssembler;

    public AiFacadeImpl(
            AiReportApplicationService aiReportApplicationService,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            DiscoveryAiApplicationService discoveryAiApplicationService,
            KnowledgeAiExtractionApplicationServiceImpl knowledgeAiExtractionApplicationService,
            AiInvocationRepository aiInvocationRepository,
            AiCandidateDomainService aiCandidateDomainService,
            AiFacadeAssembler aiFacadeAssembler) {
        this.aiReportApplicationService = aiReportApplicationService;
        this.aiBatchJobApplicationService = aiBatchJobApplicationService;
        this.discoveryAiApplicationService = discoveryAiApplicationService;
        this.knowledgeAiExtractionDomainService = knowledgeAiExtractionApplicationService;
        this.aiInvocationRepository = aiInvocationRepository;
        this.aiCandidateDomainService = aiCandidateDomainService;
        this.aiFacadeAssembler = aiFacadeAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(aiReportApplicationService.summary(
                request.getPeriodStart(), request.getPeriodEnd(), request.getBucketType()));
    }

    @Override
    public DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                discoveryAiApplicationService.understandQuery(aiFacadeAssembler.toDomainRequest(request)));
    }

    @Override
    public DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                discoveryAiApplicationService.generateAnswer(aiFacadeAssembler.toDomainRequest(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                knowledgeAiExtractionDomainService.extractRelations(aiFacadeAssembler.toDomainRequest(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                knowledgeAiExtractionDomainService.extractGraph(aiFacadeAssembler.toDomainRequest(request)));
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request) {
        return aiFacadeAssembler.toFacadeResponse(
                knowledgeAiExtractionDomainService.extractLineage(aiFacadeAssembler.toDomainRequest(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public AiBatchJobFacadeResponse getBatchJob(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(aiBatchJobApplicationService.get(batchId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request) {
        if (request == null) {
            return null;
        }
        Long batchId = aiBatchJobApplicationService.create(new AiBatchJobCreateCommand(
                request.getScope(),
                request.getCapability(),
                request.getContentType(),
                request.getTotalCount(),
                request.getFailureSummaryJson()));
        return aiFacadeAssembler.toActionResponse(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDispatchNextBatchUnit(Long batchId) {
        return aiBatchJobApplicationService.canDispatchNextUnit(batchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchSuccess(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(aiBatchJobApplicationService.recordSuccess(batchId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.recordFailure(request.getBatchId(), request.getFailureSummaryJson()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse cancelBatchJob(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(aiBatchJobApplicationService.cancel(batchId));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCallRecordFacadeDto getCallRecord(GetAiCallRecordFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiInvocationRepository.getCallRecord(request.getCallId()));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiInvocationRepository.getCandidate(request.getCandidateId()));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request) {
        return aiFacadeAssembler.toFacadeDto(
                aiCandidateDomainService.requirePendingForApply(aiFacadeAssembler.toDomainCheck(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiCandidateDomainService.markApplied(
                aiFacadeAssembler.toCandidateId(request),
                request.getResultFormat(),
                request.getResultPayload(),
                request.getAppliedAt()));
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("AiFacade bridge not implemented yet");
    }
}
