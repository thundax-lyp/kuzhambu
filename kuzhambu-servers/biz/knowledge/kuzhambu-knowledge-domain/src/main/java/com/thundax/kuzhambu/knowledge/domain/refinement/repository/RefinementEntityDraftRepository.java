package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import java.util.List;

public interface RefinementEntityDraftRepository {

    List<RefinementEntityDraft> listByTaskId(Long refinementTaskId);

    void batchSaveOrUpdate(List<RefinementEntityDraft> drafts);

    int deleteByTaskId(Long refinementTaskId);
}
