package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import java.util.Collection;
import java.util.List;

public interface KnowledgeLineageNodeRepository {

    List<KnowledgeLineageNode> listByNodeKeys(Collection<String> nodeKeys);

    void saveOrUpdateBatch(List<KnowledgeLineageNode> nodes);
}
