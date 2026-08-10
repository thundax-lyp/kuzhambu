package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.RefinementRelationDraftPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementRelationDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementRelationDraftMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RefinementRelationDraftRepositoryImpl implements RefinementRelationDraftRepository {

    private final RefinementRelationDraftMapper mapper;

    public RefinementRelationDraftRepositoryImpl(RefinementRelationDraftMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<RefinementRelationDraft> listByTaskId(Long refinementTaskId) {
        QueryWrapper<RefinementRelationDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId)
                .orderByAsc("sort_order")
                .orderByAsc("id");
        return RefinementRelationDraftPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void batchSaveOrUpdate(List<RefinementRelationDraft> drafts) {
        for (RefinementRelationDraft draft : drafts == null ? List.<RefinementRelationDraft>of() : drafts) {
            RefinementRelationDraftDO dataObject = RefinementRelationDraftPersistenceAssembler.toObject(draft);
            if (dataObject.getDraftId() == null) {
                dataObject.setDraftId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<RefinementRelationDraftDO>()
                            .eq("refinement_task_id", dataObject.getRefinementTaskId())
                            .eq("relation_key", dataObject.getRelationKey())
                            .set("relation_id", dataObject.getRelationId())
                            .set("origin_type", dataObject.getOriginType())
                            .set("operation_type", dataObject.getOperationType())
                            .set("source_entity_key", dataObject.getSourceEntityKey())
                            .set("target_entity_key", dataObject.getTargetEntityKey())
                            .set("source_name", dataObject.getSourceName())
                            .set("target_name", dataObject.getTargetName())
                            .set("relation_type", dataObject.getRelationType())
                            .set("evidence", dataObject.getEvidence())
                            .set("confirmation_status", dataObject.getConfirmationStatus())
                            .set("source_refs_json", dataObject.getSourceRefsJson())
                            .set("sort_order", dataObject.getSortOrder())
                            .set("updated_by", dataObject.getUpdatedBy())
                            .set("updated_at", dataObject.getUpdatedAt()));
            if (updated == 0) {
                mapper.insert(dataObject);
            }
        }
    }

    @Override
    public int deleteByTaskId(Long refinementTaskId) {
        QueryWrapper<RefinementRelationDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId);
        return mapper.delete(wrapper);
    }
}
