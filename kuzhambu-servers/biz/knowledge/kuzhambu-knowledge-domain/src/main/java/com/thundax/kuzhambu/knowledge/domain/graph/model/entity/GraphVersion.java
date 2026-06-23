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
public class GraphVersion {
    private Long id;
    private Long versionId;
    private GraphExtractionTaskId taskId;
    private Long candidateId;
    private String taskType;
    private String scopeType;
    private String scopeJson;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Integer versionNo;
    private String status;
    private Date appliedAt;
}
