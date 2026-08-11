package com.thundax.kuzhambu.discovery.application.qa.command;

public record DeleteQaSessionCommand(Long sessionId, String ownerType, String ownerId, Boolean adminOperation) {}
