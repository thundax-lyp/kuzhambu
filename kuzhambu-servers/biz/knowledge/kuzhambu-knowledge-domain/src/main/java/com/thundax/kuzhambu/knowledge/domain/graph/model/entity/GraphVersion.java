package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphVersion {
    private GraphVersionId id;
    private GraphExtractionTaskId taskId;
    private GraphExtractionAiCandidateId candidateId;
    private GraphExtractionTaskType taskType;
    private String scopeType;
    private String scopeJson;
    private String sourceContentType;
    private GraphExtractionSourceContentId sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Integer versionNo;
    private GraphVersionStatus status;
    private Instant appliedAt;
}
