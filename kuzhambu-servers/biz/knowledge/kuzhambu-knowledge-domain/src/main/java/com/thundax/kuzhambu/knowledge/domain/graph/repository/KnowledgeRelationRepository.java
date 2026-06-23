package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import java.util.Collection;
import java.util.List;

public interface KnowledgeRelationRepository {

    List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys);

    void saveOrUpdateBatch(List<KnowledgeRelation> relations);
}
