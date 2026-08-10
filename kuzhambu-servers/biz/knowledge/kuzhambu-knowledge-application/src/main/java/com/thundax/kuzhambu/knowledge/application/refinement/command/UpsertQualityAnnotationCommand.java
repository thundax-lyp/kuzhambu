package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record UpsertQualityAnnotationCommand(
        Long annotationId,
        String objectType,
        String objectKey,
        String sourceContentType,
        Long sourceContentId,
        Long graphVersionId,
        String annotationStatus,
        String annotationLabel,
        String comment,
        Long operatorId) {}
