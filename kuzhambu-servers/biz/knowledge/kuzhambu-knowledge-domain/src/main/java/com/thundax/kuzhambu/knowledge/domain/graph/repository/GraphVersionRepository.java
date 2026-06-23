package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface GraphVersionRepository {

    GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId);

    GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId);

    Long save(GraphVersion entity);
}
