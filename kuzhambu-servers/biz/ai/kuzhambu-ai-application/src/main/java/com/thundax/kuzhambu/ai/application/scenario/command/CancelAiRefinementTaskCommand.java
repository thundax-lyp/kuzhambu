package com.thundax.kuzhambu.ai.application.scenario.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public record CancelAiRefinementTaskCommand(AiBatchJobId taskId) {}
