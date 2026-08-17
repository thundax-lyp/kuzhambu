package com.thundax.kuzhambu.ai.application.facade.impl;

import com.thundax.kuzhambu.ai.application.facade.assembler.AiFacadeAssembler;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeGraphExtractionTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.DiscoveryAiStreamHandler;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobQueryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CleanupKnowledgeGraphCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiInvocationLogFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeGraphExtractionJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobPageFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.CleanupKnowledgeGraphCandidateFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiFacadeImpl implements AiFacade {

    private final AiReportApplicationService aiReportApplicationService;
    private final AiBatchJobApplicationService aiBatchJobApplicationService;
    private final DiscoveryAiApplicationService discoveryAiApplicationService;
    private final KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService;
    private final KnowledgeGraphExtractionTaskApplicationService knowledgeGraphExtractionTaskApplicationService;
    private final AiCandidateApplicationService aiCandidateApplicationService;
    private final AiInvocationRepository aiInvocationRepository;
    private final AiFacadeAssembler aiFacadeAssembler;

    public AiFacadeImpl(
            AiReportApplicationService aiReportApplicationService,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            DiscoveryAiApplicationService discoveryAiApplicationService,
            KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService,
            KnowledgeGraphExtractionTaskApplicationService knowledgeGraphExtractionTaskApplicationService,
            AiCandidateApplicationService aiCandidateApplicationService,
            AiInvocationRepository aiInvocationRepository,
            AiFacadeAssembler aiFacadeAssembler) {
        this.aiReportApplicationService = aiReportApplicationService;
        this.aiBatchJobApplicationService = aiBatchJobApplicationService;
        this.discoveryAiApplicationService = discoveryAiApplicationService;
        this.knowledgeAiExtractionApplicationService = knowledgeAiExtractionApplicationService;
        this.knowledgeGraphExtractionTaskApplicationService = knowledgeGraphExtractionTaskApplicationService;
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
        return aiFacadeAssembler.toFacadeResponse(
                aiReportApplicationService.summary(aiFacadeAssembler.toReportSummaryQuery(request)));
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
                aiBatchJobApplicationService.get(aiFacadeAssembler.toGetBatchJobQuery(batchId)));
    }

    @Override
    @Transactional(readOnly = true)
    public AiBatchJobFacadeResponse getLatestBatchJob(AiBatchJobQueryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        PageResult<AiBatchJobResult> result = aiBatchJobApplicationService.page(
                aiFacadeAssembler.toBatchJobsQuery(request), aiFacadeAssembler.toLatestPageQuery());
        return result.getRecords().isEmpty()
                ? null
                : aiFacadeAssembler.toFacadeResponse(result.getRecords().get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public AiBatchJobPageFacadeResponse pageBatchJobs(AiBatchJobQueryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(aiBatchJobApplicationService.page(
                aiFacadeAssembler.toBatchJobsQuery(request), aiFacadeAssembler.toPageQuery(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request) {
        if (request == null) {
            return null;
        }
        Long batchId = AiBatchJobIdCodec.toValue(
                aiBatchJobApplicationService.create(aiFacadeAssembler.toCreateBatchJobCommand(request)));
        return aiFacadeAssembler.toActionResponse(batchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobActionFacadeResponse submitKnowledgeGraphExtraction(
            KnowledgeGraphExtractionJobFacadeRequest request) {
        if (request == null) {
            return null;
        }
        Long batchId = AiBatchJobIdCodec.toValue(knowledgeGraphExtractionTaskApplicationService.submitGraph(
                aiFacadeAssembler.toKnowledgeGraphExtractionCommand(request)));
        return aiFacadeAssembler.toActionResponse(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDispatchNextBatchUnit(Long batchId) {
        return aiBatchJobApplicationService.canDispatchNextUnit(
                aiFacadeAssembler.toCanDispatchNextBatchUnitQuery(batchId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchSuccess(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.recordSuccess(aiFacadeAssembler.toRecordBatchJobCommand(batchId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.recordFailure(aiFacadeAssembler.toRecordBatchJobFailureCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobFacadeResponse cancelBatchJob(Long batchId) {
        return aiFacadeAssembler.toFacadeResponse(
                aiBatchJobApplicationService.cancel(aiFacadeAssembler.toCancelBatchJobCommand(batchId)));
    }

    @Override
    @Transactional(readOnly = true)
    public AiInvocationLogFacadeDto getInvocationLog(GetAiInvocationLogFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toFacadeDto(aiInvocationRepository.getByCallId(AiCallIdCodec.toDomain(request.getCallId())));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto getLatestCandidateByBatch(Long batchId) {
        if (batchId == null) {
            return null;
        }
        List<AiCandidate> candidates =
                aiInvocationRepository.listCandidatesByBatch(AiBatchJobIdCodec.toDomain(batchId));
        return candidates == null || candidates.isEmpty() ? null : toFacadeDto(candidates.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toFacadeDto(
                aiInvocationRepository.getByCandidateId(AiCandidateIdCodec.toDomain(request.getCandidateId())));
    }

    @Override
    @Transactional(readOnly = true)
    public AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(aiCandidateApplicationService.requirePendingForApply(
                aiFacadeAssembler.toRequirePendingCandidateQuery(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(
                aiCandidateApplicationService.markApplied(aiFacadeAssembler.toApplyCandidateCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateFacadeDto rejectCandidate(RejectAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return aiFacadeAssembler.toFacadeDto(
                aiCandidateApplicationService.reject(aiFacadeAssembler.toRejectCandidateCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CleanupKnowledgeGraphCandidateFacadeResponse cleanupKnowledgeGraphCandidate(
            CleanupKnowledgeGraphCandidateFacadeRequest request) {
        if (request == null || request.getCandidateId() == null) {
            return null;
        }
        aiCandidateApplicationService.cleanup(AiCandidateIdCodec.toDomain(request.getCandidateId()));
        return CleanupKnowledgeGraphCandidateFacadeResponse.builder()
                .candidateId(request.getCandidateId())
                .cleaned(true)
                .build();
    }

    private AiInvocationLogFacadeDto toFacadeDto(AiInvocationLog invocationLog) {
        return invocationLog == null ? null : aiFacadeAssembler.toFacadeDto(invocationLog);
    }

    private AiCandidateFacadeDto toFacadeDto(AiCandidate candidate) {
        return candidate == null ? null : aiFacadeAssembler.toFacadeDto(candidate);
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("AiFacade bridge not implemented yet");
    }
}
