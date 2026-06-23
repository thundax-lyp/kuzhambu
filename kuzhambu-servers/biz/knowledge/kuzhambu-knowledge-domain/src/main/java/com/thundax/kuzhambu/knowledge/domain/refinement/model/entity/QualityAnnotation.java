package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityAnnotation {
    private Long id;
    private Long annotationId;
    private String objectType;
    private String objectKey;
    private String sourceContentType;
    private Long sourceContentId;
    private Long graphVersionId;
    private String annotationStatus;
    private String annotationLabel;
    private String comment;
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;
}
