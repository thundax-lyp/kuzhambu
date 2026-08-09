package com.thundax.kuzhambu.knowledge.application.refinement.query;

public record RefinementWorkbenchQuery(
        String taskType, String sourceContentType, Long sourceContentId, String sourceCategoryCode, String status) {}
