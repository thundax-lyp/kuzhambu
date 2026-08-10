package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.RefinementLineageRelationDraftPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageRelationDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementLineageRelationDraftMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RefinementLineageRelationDraftRepositoryImpl implements RefinementLineageRelationDraftRepository {

    private final RefinementLineageRelationDraftMapper mapper;

    public RefinementLineageRelationDraftRepositoryImpl(RefinementLineageRelationDraftMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<RefinementLineageRelationDraft> listByTaskId(Long refinementTaskId) {
        QueryWrapper<RefinementLineageRelationDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId)
                .orderByAsc("sort_order")
                .orderByAsc("id");
        return RefinementLineageRelationDraftPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void batchSaveOrUpdate(List<RefinementLineageRelationDraft> drafts) {
        for (RefinementLineageRelationDraft draft :
                drafts == null ? List.<RefinementLineageRelationDraft>of() : drafts) {
            RefinementLineageRelationDraftDO dataObject =
                    RefinementLineageRelationDraftPersistenceAssembler.toObject(draft);
            if (dataObject.getDraftId() == null) {
                dataObject.setDraftId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<RefinementLineageRelationDraftDO>()
                            .eq("refinement_task_id", dataObject.getRefinementTaskId())
                            .eq("relation_key", dataObject.getRelationKey())
                            .set("relation_id", dataObject.getRelationId())
                            .set("origin_type", dataObject.getOriginType())
                            .set("operation_type", dataObject.getOperationType())
                            .set("source_node_key", dataObject.getSourceNodeKey())
                            .set("target_node_key", dataObject.getTargetNodeKey())
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
        QueryWrapper<RefinementLineageRelationDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId);
        return mapper.delete(wrapper);
    }
}
