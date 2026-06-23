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
}
