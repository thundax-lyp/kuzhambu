package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.RefinementLineageNodeDraftPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageNodeDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementLineageNodeDraftMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RefinementLineageNodeDraftRepositoryImpl implements RefinementLineageNodeDraftRepository {

    private final RefinementLineageNodeDraftMapper mapper;

    public RefinementLineageNodeDraftRepositoryImpl(RefinementLineageNodeDraftMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<RefinementLineageNodeDraft> listByTaskId(Long refinementTaskId) {
        QueryWrapper<RefinementLineageNodeDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId)
                .orderByAsc("sort_order")
                .orderByAsc("id");
        return RefinementLineageNodeDraftPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void batchSaveOrUpdate(List<RefinementLineageNodeDraft> drafts) {
        for (RefinementLineageNodeDraft draft : drafts == null ? List.<RefinementLineageNodeDraft>of() : drafts) {
            RefinementLineageNodeDraftDO dataObject = RefinementLineageNodeDraftPersistenceAssembler.toObject(draft);
            if (dataObject.getDraftId() == null) {
                dataObject.setDraftId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<RefinementLineageNodeDraftDO>()
                            .eq("refinement_task_id", dataObject.getRefinementTaskId())
                            .eq("node_key", dataObject.getNodeKey())
                            .set("node_id", dataObject.getNodeId())
                            .set("origin_type", dataObject.getOriginType())
                            .set("operation_type", dataObject.getOperationType())
                            .set("name", dataObject.getName())
                            .set("node_type", dataObject.getNodeType())
                            .set("generation", dataObject.getGeneration())
                            .set("gender", dataObject.getGender())
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
        QueryWrapper<RefinementLineageNodeDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId);
        return mapper.delete(wrapper);
    }
}
