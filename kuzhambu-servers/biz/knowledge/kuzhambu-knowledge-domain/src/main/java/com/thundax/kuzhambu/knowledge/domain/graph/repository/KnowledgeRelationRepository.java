package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import java.util.Collection;
import java.util.List;

public interface KnowledgeRelationRepository {

    List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys);

    KnowledgeRelation getByRelationId(Long relationId);

    PageResult<KnowledgeRelation> page(
            Long versionId, String keyword, String relationType, String confirmationStatus, int pageNo, int pageSize);

    void saveOrUpdateBatch(List<KnowledgeRelation> relations);
}
