package com.thundax.kuzhambu.discovery.application.search.query;

public record SearchPreviewQuery(
        String contentType,
        String contentId,
        String operatorType,
        String operatorId,
        String requestId,
        String traceId) {}
