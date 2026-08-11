package com.thundax.kuzhambu.discovery.application.qa.command;

public record OpenQaSessionCommand(
        Long ownerUserId,
        String title,
        String scope,
        String contextMode,
        String contextContentType,
        Long contextContentId,
        String requestId,
        String traceId) {}
