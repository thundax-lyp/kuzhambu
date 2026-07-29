package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.function.Consumer;

public interface AiRefinementTaskApplicationService {

    AiRefinementTaskResult submitRefinementTask(SubmitAiRefinementTaskCommand command);

    AiRefinementTaskResult getRefinementTask(GetAiRefinementTaskQuery query);

    PageResult<AiRefinementTaskResult> pageRefinementTasks(PageAiRefinementTasksQuery query);

    void subscribeRefinementTaskEvents(
            SubscribeAiRefinementTaskEventsQuery query, Consumer<AiStreamEventResult> eventConsumer);

    AiRefinementTaskResult cancelRefinementTask(CancelAiRefinementTaskCommand command);
}
