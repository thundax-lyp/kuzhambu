package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import java.util.List;

public interface RefinementLineageNodeDraftRepository {

    List<RefinementLineageNodeDraft> listByTaskId(Long refinementTaskId);

    void saveOrUpdateBatch(List<RefinementLineageNodeDraft> drafts);

    int deleteByTaskId(Long refinementTaskId);
}
