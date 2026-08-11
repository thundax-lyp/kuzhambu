package com.thundax.kuzhambu.discovery.application.search.command;

public record SearchIndexSyncUpsertCommand(String contentType, String contentId, Integer currentVersionNo) {}
