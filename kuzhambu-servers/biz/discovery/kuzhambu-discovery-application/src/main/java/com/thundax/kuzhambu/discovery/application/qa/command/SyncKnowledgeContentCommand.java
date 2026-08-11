package com.thundax.kuzhambu.discovery.application.qa.command;

public record SyncKnowledgeContentCommand(
        String contentType, Long contentId, Integer currentVersionNo, String requestId, String traceId) {}
