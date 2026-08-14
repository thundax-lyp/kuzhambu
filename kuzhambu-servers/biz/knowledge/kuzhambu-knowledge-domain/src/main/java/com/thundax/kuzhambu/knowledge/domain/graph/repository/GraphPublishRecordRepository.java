package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishRecord;

public interface GraphPublishRecordRepository {
    Long insert(GraphPublishRecord record);
}
