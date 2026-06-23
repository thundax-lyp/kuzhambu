package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityAnnotationResult {
    private Long annotationId;
    private String objectType;
    private String objectKey;
    private Long graphVersionId;
    private String annotationStatus;
    private String annotationLabel;
    private String comment;
}
