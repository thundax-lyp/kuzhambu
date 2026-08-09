package com.thundax.kuzhambu.ai.application.scenario.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public record GetAiRefinementTaskQuery(AiBatchJobId taskId) {}
