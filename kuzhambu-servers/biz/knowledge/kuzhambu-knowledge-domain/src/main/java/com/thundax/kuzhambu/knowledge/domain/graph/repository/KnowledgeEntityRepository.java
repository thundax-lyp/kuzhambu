package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import java.util.Collection;
import java.util.List;

public interface KnowledgeEntityRepository {

    List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys);

    KnowledgeEntity getByEntityId(KnowledgeEntityId entityId);

    default KnowledgeEntity getByEntityKey(String entityKey) {
        return null;
    }

    void batchSaveOrUpdate(List<KnowledgeEntity> entities);

    int deleteByEntityKeys(Collection<String> entityKeys);
}
