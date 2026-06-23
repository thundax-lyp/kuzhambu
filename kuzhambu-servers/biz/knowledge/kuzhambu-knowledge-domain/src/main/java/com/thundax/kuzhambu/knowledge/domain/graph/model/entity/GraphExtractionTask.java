package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionTask {
    private GraphExtractionTaskId id;
    private GraphExtractionTaskId taskId;
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
    private Date requestedAt;
    private Date completedAt;
    private Date appliedAt;
}
