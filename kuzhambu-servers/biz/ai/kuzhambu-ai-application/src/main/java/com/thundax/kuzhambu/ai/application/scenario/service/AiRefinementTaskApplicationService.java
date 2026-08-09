package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.query.AiRefinementTasksQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.function.Consumer;

public interface AiRefinementTaskApplicationService {

    AiRefinementTaskResult submit(SubmitAiRefinementTaskCommand command);

    AiRefinementTaskResult get(GetAiRefinementTaskQuery query);

    PageResult<AiRefinementTaskResult> page(AiRefinementTasksQuery query, PageQuery pageQuery);

    void subscribeEvents(SubscribeAiRefinementTaskEventsQuery query, Consumer<AiStreamEventResult> eventConsumer);

    AiRefinementTaskResult cancel(CancelAiRefinementTaskCommand command);
}
