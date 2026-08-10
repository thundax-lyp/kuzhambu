package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import java.util.Collection;
import java.util.List;

public interface KnowledgeEntityRepository {

    List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys);

    List<KnowledgeEntity> listByVersionId(GraphVersionId versionId);

    KnowledgeEntity getByEntityId(KnowledgeEntityId entityId);

    default KnowledgeEntity getByEntityKey(String entityKey) {
        return null;
    }

    PageResult<KnowledgeEntity> page(
            GraphVersionId versionId,
            String keyword,
            String entityType,
            KnowledgeConfirmationStatus confirmationStatus,
            int pageNo,
            int pageSize);

    void batchSaveOrUpdate(List<KnowledgeEntity> entities);

    int deleteByEntityKeys(Collection<String> entityKeys);
}
