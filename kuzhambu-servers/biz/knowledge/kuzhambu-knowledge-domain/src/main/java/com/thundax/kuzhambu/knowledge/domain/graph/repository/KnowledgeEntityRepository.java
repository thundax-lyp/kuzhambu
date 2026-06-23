package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import java.util.Collection;
import java.util.List;

public interface KnowledgeEntityRepository {

    List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys);

    void saveOrUpdateBatch(List<KnowledgeEntity> entities);
}
