package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;

public interface RefinementTaskRepository {

    RefinementTask getByTaskId(RefinementTaskId taskId);

    RefinementTask findLatestDraft(
            String taskType, String sourceContentType, Long sourceContentId, Long graphVersionId);

    PageResult<RefinementTask> page(
            String taskType,
            String sourceContentType,
            Long sourceContentId,
            String sourceCategoryCode,
            String status,
            int pageNo,
            int pageSize);

    Long save(RefinementTask entity);

    int update(RefinementTask entity);
}
