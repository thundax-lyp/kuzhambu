package com.thundax.kuzhambu.ai.application.scenario.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelAiRefinementTaskCommand {

    private final AiBatchJobId taskId;
}
