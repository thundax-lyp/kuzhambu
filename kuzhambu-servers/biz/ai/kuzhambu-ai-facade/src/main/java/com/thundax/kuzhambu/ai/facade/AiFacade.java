package com.thundax.kuzhambu.ai.facade;

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

public interface AiFacade {

    AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request);

    DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request);

    DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(KnowledgeAiExtractionFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request);

    AiBatchJobFacadeResponse getBatchJob(Long batchId);

    AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request);

    boolean canDispatchNextBatchUnit(Long batchId);

    AiBatchJobFacadeResponse recordBatchSuccess(Long batchId);

    AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request);

    AiBatchJobFacadeResponse cancelBatchJob(Long batchId);

    AiCallRecordFacadeDto getCallRecord(GetAiCallRecordFacadeRequest request);

    AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request);

    AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request);

    AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request);
}
