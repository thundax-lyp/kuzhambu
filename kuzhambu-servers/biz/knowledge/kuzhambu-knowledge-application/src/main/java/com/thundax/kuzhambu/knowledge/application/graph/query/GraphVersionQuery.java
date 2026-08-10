package com.thundax.kuzhambu.knowledge.application.graph.query;

public record GraphVersionQuery(String taskType, String status, String sourceContentType, Long sourceContentId) {}
