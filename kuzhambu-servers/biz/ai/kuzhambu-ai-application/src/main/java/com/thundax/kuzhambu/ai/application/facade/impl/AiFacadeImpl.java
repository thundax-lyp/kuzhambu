package com.thundax.kuzhambu.ai.application.facade.impl;

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

@Service
public class AiFacadeImpl implements AiFacade {

    @Override
    public AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(KnowledgeAiExtractionFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public AiBatchJobFacadeResponse getBatchJob(Long batchId) {
        throw unsupported();
    }

    @Override
    public AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public boolean canDispatchNextBatchUnit(Long batchId) {
        throw unsupported();
    }

    @Override
    public AiBatchJobFacadeResponse recordBatchSuccess(Long batchId) {
        throw unsupported();
    }

    @Override
    public AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public AiBatchJobFacadeResponse cancelBatchJob(Long batchId) {
        throw unsupported();
    }

    @Override
    public AiCallRecordFacadeDto getCallRecord(GetAiCallRecordFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request) {
        throw unsupported();
    }

    @Override
    public AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("AiFacade bridge not implemented yet");
    }
}
