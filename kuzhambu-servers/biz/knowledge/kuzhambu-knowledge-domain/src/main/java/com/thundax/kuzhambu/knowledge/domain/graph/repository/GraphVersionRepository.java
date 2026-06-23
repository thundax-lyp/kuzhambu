package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface GraphVersionRepository {

    GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId);

    GraphVersion getByVersionId(Long versionId);

    GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId);

    PageResult<GraphVersion> page(
            String taskType, String status, String sourceContentType, Long sourceContentId, int pageNo, int pageSize);

    Long save(GraphVersion entity);
}
