package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeGraphExtractionTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class KnowledgeGraphExtractionTaskApplicationServiceImpl
        implements KnowledgeGraphExtractionTaskApplicationService {

    private static final String TASK_TYPE_GRAPH = "GRAPH";

    private final AiBatchJobApplicationService aiBatchJobApplicationService;

    public KnowledgeGraphExtractionTaskApplicationServiceImpl(
            AiBatchJobApplicationService aiBatchJobApplicationService) {
        this.aiBatchJobApplicationService = aiBatchJobApplicationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobId submitGraph(KnowledgeAiExtractionCommand command) {
        validate(command);
        return aiBatchJobApplicationService.create(new AiBatchJobCreateCommand(
                command.scopeType(),
                AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                AiContentRef.ofNullable(command.sourceContentType(), command.sourceContentId()),
                1,
                null));
    }

    private void validate(KnowledgeAiExtractionCommand command) {
        if (command == null
                || !TASK_TYPE_GRAPH.equals(command.taskType())
                || isBlank(command.scopeType())
                || isBlank(command.sourceContentType())
                || command.sourceContentId() == null
                || isBlank(command.scopeJson())
                || command.requestedBy() == null
                || isBlank(command.inputPayloadJson())) {
            throw new BizException("Knowledge graph extraction job request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
