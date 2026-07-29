package com.thundax.kuzhambu.ai.application.scenario.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscribeAiRefinementTaskEventsQuery {

    private final AiBatchJobId taskId;
}
