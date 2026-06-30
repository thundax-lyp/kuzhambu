package com.thundax.kuzhambu.ai.domain.refinement.repository;

import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import java.time.Instant;
import java.util.List;

public interface AiRefinementTaskRepository {

    AiRefinementTask getTask(Long taskId);

    Long saveTask(AiRefinementTask task);

    int updateTask(AiRefinementTask task);

    List<AiRefinementTask> listTasks(
            String capability,
            String status,
            String contentType,
            Long contentId,
            Long requestedBy,
            Integer pageNo,
            Integer pageSize);

    long countTasks(String capability, String status, String contentType, Long contentId, Long requestedBy);

    List<AiRefinementTask> listExpiredRunningTasks(Instant threshold);

    int deleteExpiredTerminalTasks(Instant threshold);
}
