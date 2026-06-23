package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertQualityAnnotationCommand {
    private Long annotationId;
    private String objectType;
    private String objectKey;
    private String sourceContentType;
    private Long sourceContentId;
    private Long graphVersionId;
    private String annotationStatus;
    private String annotationLabel;
    private String comment;
    private Long operatorId;
}
