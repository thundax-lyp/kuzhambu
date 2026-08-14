package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphPublicationConflictDecision(
        String objectType, Long materialObjectId, String action, Long matchedObjectId) {}
