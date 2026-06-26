package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;

public final class KnowledgeGraphExtractionInterfaceAssembler {

    private KnowledgeGraphExtractionInterfaceAssembler() {}

    public static RequestRelationExtractionCommand toRelationCommand(GraphExtractionRequests.CreateRequest request) {
        RequestRelationExtractionCommand command = new RequestRelationExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
        command.setTriggerSource(request == null ? null : request.getTriggerSource());
        command.setSelectionScopeJson(request == null ? null : request.getSelectionScopeJson());
        command.setReplaceUnconfirmedOnly(request == null ? null : request.getReplaceUnconfirmedOnly());
        return command;
    }

    public static RequestGraphExtractionCommand toGraphCommand(GraphExtractionRequests.CreateRequest request) {
        RequestGraphExtractionCommand command = new RequestGraphExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
        command.setTriggerSource(request == null ? null : request.getTriggerSource());
        command.setSelectionScopeJson(request == null ? null : request.getSelectionScopeJson());
        command.setReplaceUnconfirmedOnly(request == null ? null : request.getReplaceUnconfirmedOnly());
        return command;
    }

    public static RequestLineageExtractionCommand toLineageCommand(GraphExtractionRequests.CreateRequest request) {
        RequestLineageExtractionCommand command = new RequestLineageExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
        command.setTriggerSource(request == null ? null : request.getTriggerSource());
        command.setSelectionScopeJson(request == null ? null : request.getSelectionScopeJson());
        command.setReplaceUnconfirmedOnly(request == null ? null : request.getReplaceUnconfirmedOnly());
        return command;
    }

    public static GraphExtractionTaskId toTaskId(GraphExtractionRequests.TaskIdRequest request) {
        return request == null ? null : GraphExtractionTaskId.ofNullable(request.getTaskId());
    }

    public static GraphExtractionResponses.TaskResponse toResponse(GraphExtractionTaskResult result) {
        return GraphExtractionResponses.TaskResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .batchJobId(result == null ? null : result.getBatchJobId())
                .taskType(result == null ? null : result.getTaskType())
                .scopeType(result == null ? null : result.getScopeType())
                .scopeJson(result == null ? null : result.getScopeJson())
                .triggerSource(result == null ? null : result.getTriggerSource())
                .selectionScopeJson(result == null ? null : result.getSelectionScopeJson())
                .replaceUnconfirmedOnly(result == null ? null : result.getReplaceUnconfirmedOnly())
                .parentTaskId(result == null ? null : result.getParentTaskId())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .aiCallId(result == null ? null : result.getAiCallId())
                .aiCandidateId(result == null ? null : result.getAiCandidateId())
                .status(result == null ? null : result.getStatus())
                .errorType(result == null ? null : result.getErrorType())
                .errorMessage(result == null ? null : result.getErrorMessage())
                .requestedBy(result == null ? null : result.getRequestedBy())
                .requestedAt(result == null ? null : result.getRequestedAt())
                .completedAt(result == null ? null : result.getCompletedAt())
                .appliedAt(result == null ? null : result.getAppliedAt())
                .build();
    }

    public static GraphExtractionResponses.BatchCancelResponse toResponse(GraphExtractionBatchCancelResult result) {
        return GraphExtractionResponses.BatchCancelResponse.builder()
                .batchJobId(result == null ? null : result.getBatchJobId())
                .status(result == null ? null : result.getStatus())
                .cancelledCount(result == null ? null : result.getCancelledCount())
                .completedCount(result == null ? null : result.getCompletedCount())
                .failedCount(result == null ? null : result.getFailedCount())
                .build();
    }

    public static GraphExtractionResponses.VersionResponse toResponse(GraphVersionResult result) {
        return GraphExtractionResponses.VersionResponse.builder()
                .versionId(result == null ? null : result.getVersionId())
                .taskId(result == null ? null : result.getTaskId())
                .candidateId(result == null ? null : result.getCandidateId())
                .taskType(result == null ? null : result.getTaskType())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .versionNo(result == null ? null : result.getVersionNo())
                .status(result == null ? null : result.getStatus())
                .appliedAt(result == null ? null : result.getAppliedAt())
                .build();
    }

    public static GraphExtractionResponses.EntityResponse toResponse(KnowledgeEntityResult result) {
        return GraphExtractionResponses.EntityResponse.builder()
                .entityId(result == null ? null : result.getEntityId())
                .entityKey(result == null ? null : result.getEntityKey())
                .name(result == null ? null : result.getName())
                .entityType(result == null ? null : result.getEntityType())
                .description(result == null ? null : result.getDescription())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.RelationResponse toResponse(KnowledgeRelationResult result) {
        return GraphExtractionResponses.RelationResponse.builder()
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .sourceName(result == null ? null : result.getSourceName())
                .targetName(result == null ? null : result.getTargetName())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.LineageNodeResponse toResponse(KnowledgeLineageNodeResult result) {
        return GraphExtractionResponses.LineageNodeResponse.builder()
                .nodeId(result == null ? null : result.getNodeId())
                .nodeKey(result == null ? null : result.getNodeKey())
                .name(result == null ? null : result.getName())
                .nodeType(result == null ? null : result.getNodeType())
                .generation(result == null ? null : result.getGeneration())
                .gender(result == null ? null : result.getGender())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.LineageRelationResponse toResponse(KnowledgeLineageRelationResult result) {
        return GraphExtractionResponses.LineageRelationResponse.builder()
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .sourceName(result == null ? null : result.getSourceName())
                .targetName(result == null ? null : result.getTargetName())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }
}
