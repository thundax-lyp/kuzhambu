package com.thundax.kuzhambu.classics.application.sancai.command;

import java.time.Instant;

public record SancaiDraftCommand(Long entryId, Instant autosavedAt, String draftJson) {}
