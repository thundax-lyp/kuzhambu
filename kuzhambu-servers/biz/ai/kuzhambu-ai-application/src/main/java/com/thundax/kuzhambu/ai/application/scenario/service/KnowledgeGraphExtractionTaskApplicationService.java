package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public interface KnowledgeGraphExtractionTaskApplicationService {

    AiBatchJobId submitGraph(KnowledgeAiExtractionCommand command);
}
