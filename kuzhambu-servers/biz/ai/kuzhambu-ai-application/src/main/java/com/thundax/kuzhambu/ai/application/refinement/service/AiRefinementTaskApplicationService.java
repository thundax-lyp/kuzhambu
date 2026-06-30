package com.thundax.kuzhambu.ai.application.refinement.service;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;

public interface AiRefinementTaskApplicationService {

    AiRefinementTask addTask(AiRefinementRequestCommand command);

    AiRefinementTask getTask(Long taskId);

    PageResult<AiRefinementTask> pageTasks(
            String capability, String status, String contentType, Long contentId, Long requestedBy, PageQuery pageQuery);

    AiRefinementTask cancelTask(Long taskId, Long requestedBy);
}
