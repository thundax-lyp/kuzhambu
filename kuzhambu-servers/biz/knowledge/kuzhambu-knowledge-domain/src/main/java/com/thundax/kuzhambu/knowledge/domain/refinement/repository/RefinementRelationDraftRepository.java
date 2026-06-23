package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import java.util.List;

public interface RefinementRelationDraftRepository {

    List<RefinementRelationDraft> listByTaskId(Long refinementTaskId);

    void saveOrUpdateBatch(List<RefinementRelationDraft> drafts);

    int deleteByTaskId(Long refinementTaskId);
}
