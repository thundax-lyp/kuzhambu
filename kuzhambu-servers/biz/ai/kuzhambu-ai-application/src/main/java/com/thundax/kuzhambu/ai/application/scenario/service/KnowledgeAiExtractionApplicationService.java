package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;

public interface KnowledgeAiExtractionApplicationService {

    KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionCommand input);

    KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionCommand input);
}
