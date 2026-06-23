package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import java.util.List;

public interface RefinementLineageRelationDraftRepository {

    List<RefinementLineageRelationDraft> listByTaskId(Long refinementTaskId);

    void saveOrUpdateBatch(List<RefinementLineageRelationDraft> drafts);

    int deleteByTaskId(Long refinementTaskId);
}
