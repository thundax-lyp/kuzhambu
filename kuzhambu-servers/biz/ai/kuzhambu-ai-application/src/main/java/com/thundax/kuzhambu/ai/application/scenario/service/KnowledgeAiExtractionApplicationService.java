package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;

public interface KnowledgeAiExtractionApplicationService {

    KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionCommand command);

    KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionCommand command);

    KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionCommand command);

    KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionCommand command);
}
