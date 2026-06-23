package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;

public final class KnowledgeGraphExtractionInterfaceAssembler {

    private KnowledgeGraphExtractionInterfaceAssembler() {}

    public static RequestRelationExtractionCommand toRelationCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestRelationExtractionCommand(
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
    }

    public static RequestGraphExtractionCommand toGraphCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestGraphExtractionCommand(
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
    }

    public static RequestLineageExtractionCommand toLineageCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestLineageExtractionCommand(
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
    }

    public static GraphExtractionTaskId toTaskId(GraphExtractionRequests.TaskIdRequest request) {
        return request == null ? null : GraphExtractionTaskId.ofNullable(request.getTaskId());
    }

    public static GraphExtractionResponses.TaskResponse toResponse(GraphExtractionTaskResult result) {
        return GraphExtractionResponses.TaskResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .taskType(result == null ? null : result.getTaskType())
                .scopeType(result == null ? null : result.getScopeType())
                .scopeJson(result == null ? null : result.getScopeJson())
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
}
