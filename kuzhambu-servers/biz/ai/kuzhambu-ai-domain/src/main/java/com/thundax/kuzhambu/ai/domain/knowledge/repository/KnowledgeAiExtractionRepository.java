package com.thundax.kuzhambu.ai.domain.knowledge.repository;

import com.thundax.kuzhambu.ai.domain.knowledge.model.entity.KnowledgeAiExtractionRecord;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionInput;

public interface KnowledgeAiExtractionRepository {

    KnowledgeAiExtractionRecord extractRelations(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractGraph(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractLineage(KnowledgeAiExtractionInput input);

    KnowledgeAiExtractionRecord extractTags(KnowledgeAiExtractionInput input);
}
