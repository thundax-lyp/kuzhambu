package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import java.util.Collection;
import java.util.List;

public interface KnowledgeLineageNodeRepository {

    List<KnowledgeLineageNode> listByNodeKeys(Collection<String> nodeKeys);

    List<KnowledgeLineageNode> listByVersionId(Long versionId);

    KnowledgeLineageNode getByNodeId(Long nodeId);

    PageResult<KnowledgeLineageNode> page(
            Long versionId, String keyword, String nodeType, String confirmationStatus, int pageNo, int pageSize);

    void batchSaveOrUpdate(List<KnowledgeLineageNode> nodes);

    int deleteByNodeKeys(Collection<String> nodeKeys);
}
