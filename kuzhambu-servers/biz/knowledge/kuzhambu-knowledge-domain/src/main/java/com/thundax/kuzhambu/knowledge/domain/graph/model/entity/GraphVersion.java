package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphVersion {
    private Long id;
    private Long versionId;
    private GraphExtractionTaskId taskId;
    private Long candidateId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private Integer versionNo;
    private String status;
    private Date appliedAt;
}
