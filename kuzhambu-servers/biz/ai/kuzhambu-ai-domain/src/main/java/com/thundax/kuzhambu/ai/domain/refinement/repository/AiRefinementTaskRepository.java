package com.thundax.kuzhambu.ai.domain.refinement.repository;

import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AiRefinementTaskRepository {

    AiRefinementTask get(Long taskId);

    Long insert(AiRefinementTask task);

    int update(AiRefinementTask task);

    int updateWhenStatusIn(AiRefinementTask task, Collection<String> statuses);

    List<AiRefinementTask> listTasks(
            String capability,
            String status,
            String contentType,
            Long contentId,
            Long requestedBy,
            Integer pageNo,
            Integer pageSize);

    long countTasks(String capability, String status, String contentType, Long contentId, Long requestedBy);

    List<AiRefinementTask> listActiveTasks();

    List<AiRefinementTask> listExpiredRunningTasks(Instant threshold);

    int deleteExpiredTerminalTasks(Instant threshold);
}
