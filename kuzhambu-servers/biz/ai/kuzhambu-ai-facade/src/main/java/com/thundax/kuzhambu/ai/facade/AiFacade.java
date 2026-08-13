package com.thundax.kuzhambu.ai.facade;

import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobQueryFacadeRequest;
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
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobPageFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;

public interface AiFacade {

    AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request);

    DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request);

    DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request);

    DiscoveryAiFacadeResponse streamDiscoveryAnswer(
            DiscoveryAiFacadeRequest request, DiscoveryAiStreamHandler streamHandler);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(KnowledgeAiExtractionFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request);

    KnowledgeAiExtractionFacadeResponse extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest request);

    AiBatchJobFacadeResponse getBatchJob(Long batchId);

    AiBatchJobFacadeResponse getLatestBatchJob(AiBatchJobQueryFacadeRequest request);

    AiBatchJobPageFacadeResponse pageBatchJobs(AiBatchJobQueryFacadeRequest request);

    AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request);

    boolean canDispatchNextBatchUnit(Long batchId);

    AiBatchJobFacadeResponse recordBatchSuccess(Long batchId);

    AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request);

    AiBatchJobFacadeResponse cancelBatchJob(Long batchId);

    AiInvocationLogFacadeDto getInvocationLog(GetAiInvocationLogFacadeRequest request);

    AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request);

    AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request);

    AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request);

    AiCandidateFacadeDto rejectCandidate(RejectAiCandidateFacadeRequest request);
}
