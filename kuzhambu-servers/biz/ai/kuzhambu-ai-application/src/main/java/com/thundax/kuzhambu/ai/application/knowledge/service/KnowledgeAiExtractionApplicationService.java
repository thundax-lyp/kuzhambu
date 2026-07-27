package com.thundax.kuzhambu.ai.application.knowledge.service;

import com.thundax.kuzhambu.ai.domain.knowledge.model.entity.KnowledgeAiExtractionRecord;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionInput;

public interface KnowledgeAiExtractionApplicationService {

    KnowledgeAiExtractionRecord extractRelations(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractGraph(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractLineage(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractTags(KnowledgeAiExtractionInput input);
}
