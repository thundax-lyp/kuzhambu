package com.thundax.kuzhambu.ai.application.knowledge.service;

import com.thundax.kuzhambu.ai.application.knowledge.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.knowledge.result.KnowledgeAiExtractionResult;

public interface KnowledgeAiExtractionApplicationService {

    KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionCommand input);
}
