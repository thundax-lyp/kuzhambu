package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCallId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionBatchJobId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionModelId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionModelName;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionPromptVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionRequestId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionRequesterId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTraceId;
import java.time.Instant;
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
    private GraphExtractionBatchJobId batchJobId;
    private GraphExtractionTaskType taskType;
    private String scopeType;
    private String scopeJson;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private GraphExtractionTaskId parentTaskId;
    private String sourceContentType;
    private GraphExtractionSourceContentId sourceContentId;
    private GraphExtractionModelId modelId;
    private GraphExtractionModelName modelName;
    private GraphExtractionPromptVersionId promptVersionId;
    private GraphExtractionRequestId requestId;
    private GraphExtractionTraceId traceId;
    private String promptMessagesJson;
    private String promptVariablesJson;
    private String promptHash;
    private String inputPayloadJson;
    private String outputSchemaJson;
    private Boolean forceJson;
    private String locale;
    private GraphExtractionAiCallId aiCallId;
    private GraphExtractionAiCandidateId aiCandidateId;
    private GraphExtractionTaskStatus status;
    private String errorType;
    private String errorMessage;
    private GraphExtractionRequesterId requestedBy;
    private Instant requestedAt;
    private Instant completedAt;
    private Instant appliedAt;
}
