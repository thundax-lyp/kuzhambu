package com.thundax.kuzhambu.ai.facade;

import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
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
}
