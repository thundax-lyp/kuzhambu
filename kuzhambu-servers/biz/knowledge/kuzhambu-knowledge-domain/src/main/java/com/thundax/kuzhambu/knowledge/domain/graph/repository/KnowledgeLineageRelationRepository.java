package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import java.util.Collection;
import java.util.List;

public interface KnowledgeLineageRelationRepository {

    List<KnowledgeLineageRelation> listByRelationKeys(Collection<String> relationKeys);

    List<KnowledgeLineageRelation> listByVersionId(Long versionId);

    KnowledgeLineageRelation getByRelationId(Long relationId);

    PageResult<KnowledgeLineageRelation> page(
            Long versionId, String keyword, String relationType, String confirmationStatus, int pageNo, int pageSize);

    void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations);

    int deleteByRelationKeys(Collection<String> relationKeys);
}
