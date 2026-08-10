package com.thundax.kuzhambu.classics.application.content.command;

import java.util.List;

public record AiCandidateBatchApplyContentCommand(List<AiCandidateApplyContentCommand> items) {}
