package com.thundax.kuzhambu.knowledge.application.refinement.query;

public record QualityReportQuery(
        Long graphVersionId, String sourceContentType, Long sourceContentId, String reportStatus) {}
