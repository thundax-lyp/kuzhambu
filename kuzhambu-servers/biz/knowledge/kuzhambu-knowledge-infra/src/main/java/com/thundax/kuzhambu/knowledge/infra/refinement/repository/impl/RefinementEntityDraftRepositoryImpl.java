package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.RefinementEntityDraftPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementEntityDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementEntityDraftMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RefinementEntityDraftRepositoryImpl implements RefinementEntityDraftRepository {

    private final RefinementEntityDraftMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public RefinementEntityDraftRepositoryImpl(RefinementEntityDraftMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<RefinementEntityDraft> listByTaskId(Long refinementTaskId) {
        QueryWrapper<RefinementEntityDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId)
                .orderByAsc("sort_order")
                .orderByAsc("id");
        return RefinementEntityDraftPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdateBatch(List<RefinementEntityDraft> drafts) {
        for (RefinementEntityDraft draft : drafts == null ? List.<RefinementEntityDraft>of() : drafts) {
            RefinementEntityDraftDO dataObject = RefinementEntityDraftPersistenceAssembler.toObject(draft);
            if (dataObject.getId() == null) {
                dataObject.setId(idGenerator.nextId().value());
            }
            if (dataObject.getDraftId() == null) {
                dataObject.setDraftId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<RefinementEntityDraftDO>()
                            .eq("refinement_task_id", dataObject.getRefinementTaskId())
                            .eq("entity_key", dataObject.getEntityKey())
                            .set("entity_id", dataObject.getEntityId())
                            .set("origin_type", dataObject.getOriginType())
                            .set("operation_type", dataObject.getOperationType())
                            .set("name", dataObject.getName())
                            .set("entity_type", dataObject.getEntityType())
                            .set("description", dataObject.getDescription())
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
        QueryWrapper<RefinementEntityDraftDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", refinementTaskId);
        return mapper.delete(wrapper);
    }
}
