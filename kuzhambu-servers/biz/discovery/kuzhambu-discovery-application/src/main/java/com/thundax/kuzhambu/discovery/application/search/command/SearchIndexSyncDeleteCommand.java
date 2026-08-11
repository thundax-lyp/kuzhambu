package com.thundax.kuzhambu.discovery.application.search.command;

import java.time.Instant;

public record SearchIndexSyncDeleteCommand(
        String contentType, String contentId, Integer currentVersionNo, Instant occurredAt) {}
