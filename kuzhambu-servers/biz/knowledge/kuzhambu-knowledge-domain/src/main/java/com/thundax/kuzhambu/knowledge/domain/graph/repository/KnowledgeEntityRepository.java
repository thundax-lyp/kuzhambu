package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import java.util.Collection;
import java.util.List;

public interface KnowledgeEntityRepository {

    List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys);

    List<KnowledgeEntity> listByVersionId(Long versionId);

    KnowledgeEntity getByEntityId(Long entityId);

    default KnowledgeEntity getByEntityKey(String entityKey) {
        return null;
    }

    PageResult<KnowledgeEntity> page(
            Long versionId, String keyword, String entityType, String confirmationStatus, int pageNo, int pageSize);

    void saveOrUpdateBatch(List<KnowledgeEntity> entities);

    int deleteByEntityKeys(Collection<String> entityKeys);
}
