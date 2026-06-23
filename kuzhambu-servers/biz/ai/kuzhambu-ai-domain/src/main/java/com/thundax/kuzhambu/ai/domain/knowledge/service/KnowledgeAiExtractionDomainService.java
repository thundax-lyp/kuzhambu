package com.thundax.kuzhambu.ai.domain.knowledge.service;

import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;

public interface KnowledgeAiExtractionDomainService {

    KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionRequest request);

    KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionRequest request);

    KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionRequest request);
}
