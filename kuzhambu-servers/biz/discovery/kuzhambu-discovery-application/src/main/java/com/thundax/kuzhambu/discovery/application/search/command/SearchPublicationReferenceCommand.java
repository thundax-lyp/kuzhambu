package com.thundax.kuzhambu.discovery.application.search.command;

import java.time.Instant;

public record SearchPublicationReferenceCommand(
        String documentId, String contentVersionId, Integer contentVersionNo, Instant occurredAt) {}
