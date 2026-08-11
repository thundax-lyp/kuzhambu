package com.thundax.kuzhambu.discovery.application.qa.command;

public record ExportQaSessionCommand(
        Long sessionId,
        Long requesterUserId,
        String ownerType,
        String ownerId,
        Boolean adminOperation,
        String format) {}
