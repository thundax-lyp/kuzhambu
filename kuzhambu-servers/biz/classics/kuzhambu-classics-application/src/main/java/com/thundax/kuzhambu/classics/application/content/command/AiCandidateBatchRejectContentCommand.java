package com.thundax.kuzhambu.classics.application.content.command;

import java.util.List;

public record AiCandidateBatchRejectContentCommand(
        List<AiCandidateBatchRejectContentItemCommand> items, String errorType, String errorMessage) {}
