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
    private String taskType;
    private String scopeType;
    private String scopeJson;
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
}
