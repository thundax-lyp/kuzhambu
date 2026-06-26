package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionTaskResult {
    private String taskId;
    private Long batchJobId;
    private String taskType;
    private String scopeType;
    private String scopeJson;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private Long parentTaskId;
    private String sourceContentType;
    private Long sourceContentId;
    private Long aiCallId;
    private Long aiCandidateId;
    private String status;
    private String errorType;
    private String errorMessage;
    private Long requestedBy;
    private Long requestedAt;
    private Long completedAt;
    private Long appliedAt;

    public GraphExtractionTaskResult(
            String taskId,
            String taskType,
            String scopeType,
            String scopeJson,
            String sourceContentType,
            Long sourceContentId,
            Long aiCallId,
            Long aiCandidateId,
            String status,
            String errorType,
            String errorMessage,
            Long requestedBy,
            Long requestedAt,
            Long completedAt,
            Long appliedAt) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.scopeType = scopeType;
        this.scopeJson = scopeJson;
        this.sourceContentType = sourceContentType;
        this.sourceContentId = sourceContentId;
        this.aiCallId = aiCallId;
        this.aiCandidateId = aiCandidateId;
        this.status = status;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.requestedBy = requestedBy;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.appliedAt = appliedAt;
    }
}
