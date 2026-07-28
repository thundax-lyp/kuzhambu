package com.thundax.kuzhambu.ai.application.refinement.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.function.Consumer;

public interface AiRefinementTaskApplicationService {

    AiRefinementTaskResult addTask(AiRefinementRequestCommand command);

    AiRefinementTaskResult getTask(Long taskId);

    PageResult<AiRefinementTaskResult> pageTasks(
            String capability, String status, String contentType, Long contentId, PageQuery pageQuery);

    void streamTaskEvents(Long taskId, Consumer<AiStreamEventResult> eventConsumer);

    AiRefinementTaskResult cancelTask(Long taskId);
}
