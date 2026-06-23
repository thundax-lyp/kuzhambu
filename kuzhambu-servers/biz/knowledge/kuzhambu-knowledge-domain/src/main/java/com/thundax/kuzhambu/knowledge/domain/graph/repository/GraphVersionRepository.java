package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;

public interface GraphVersionRepository {

    GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId);

    Long save(GraphVersion entity);
}
