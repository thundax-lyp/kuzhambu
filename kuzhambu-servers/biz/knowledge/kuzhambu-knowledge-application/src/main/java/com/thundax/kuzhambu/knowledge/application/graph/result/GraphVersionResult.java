package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphVersionResult {
    private Long versionId;
    private String taskId;
    private Long candidateId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private Integer versionNo;
    private String status;
    private Long appliedAt;
    private Boolean refinementApplied;
    private Long lastRefinementTaskId;
    private Long lastRefinementAppliedAt;

    public GraphVersionResult(
            Long versionId,
            String taskId,
            Long candidateId,
            String taskType,
            String sourceContentType,
            Long sourceContentId,
            Integer versionNo,
            String status,
            Long appliedAt) {
        this(
                versionId,
                taskId,
                candidateId,
                taskType,
                sourceContentType,
                sourceContentId,
                versionNo,
                status,
                appliedAt,
                Boolean.FALSE,
                null,
                null);
    }
}
