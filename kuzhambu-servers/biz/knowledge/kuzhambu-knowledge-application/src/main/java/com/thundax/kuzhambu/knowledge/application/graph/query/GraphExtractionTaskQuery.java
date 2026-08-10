package com.thundax.kuzhambu.knowledge.application.graph.query;

public record GraphExtractionTaskQuery(
        String taskType,
        Long batchJobId,
        String triggerSource,
        String status,
        String sourceContentType,
        Long sourceContentId) {}
