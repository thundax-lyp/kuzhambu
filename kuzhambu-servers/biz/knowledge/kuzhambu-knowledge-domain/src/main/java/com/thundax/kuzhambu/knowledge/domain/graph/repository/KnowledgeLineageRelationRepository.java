package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import java.util.Collection;
import java.util.List;

public interface KnowledgeLineageRelationRepository {

    List<KnowledgeLineageRelation> listByRelationKeys(Collection<String> relationKeys);

    void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations);
}
