package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;

public interface GraphVersionRepository {

    GraphVersion findLatest(
            GraphExtractionTaskType taskType, String sourceContentType, GraphExtractionSourceContentId sourceContentId);

    default GraphVersion findLatestAppliedByCategoryCode(String sourceCategoryCode) {
        return null;
    }

    default java.util.List<GraphVersion> listAppliedByCategoryCode(String sourceCategoryCode) {
        return java.util.List.of();
    }

    GraphVersion getByVersionId(GraphVersionId versionId);

    GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, GraphExtractionAiCandidateId candidateId);

    PageResult<GraphVersion> page(
            GraphExtractionTaskType taskType,
            GraphVersionStatus status,
            String sourceContentType,
            GraphExtractionSourceContentId sourceContentId,
            int pageNo,
            int pageSize);

    GraphVersionId save(GraphVersion entity);
}
