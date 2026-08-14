package com.thundax.kuzhambu.knowledge.application.graph.result;

public record GraphValidationIssueResult(
        String code, String severity, String objectType, String objectId, String field, String message) {}
