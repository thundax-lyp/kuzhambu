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
    private Long batchJobId;
    private String taskType;
    private String scopeType;
    private String scopeJson;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private GraphExtractionTaskId parentTaskId;
    private String sourceContentType;
    private Long sourceContentId;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private String requestId;
    private String traceId;
    private String promptMessagesJson;
    private String promptVariablesJson;
    private String promptHash;
    private String inputPayloadJson;
    private String outputSchemaJson;
    private Boolean forceJson;
    private String locale;
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
