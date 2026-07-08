package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.RefinementTaskPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementTaskDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class RefinementTaskRepositoryImpl implements RefinementTaskRepository {

    private final RefinementTaskMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public RefinementTaskRepositoryImpl(RefinementTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RefinementTask getByTaskId(RefinementTaskId taskId) {
        QueryWrapper<RefinementTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq("refinement_task_id", taskId == null ? null : taskId.value());
        return RefinementTaskPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public RefinementTask findLatestDraft(
            String taskType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
        QueryWrapper<RefinementTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", taskType)
                .eq("source_content_type", sourceContentType)
                .eq("source_content_id", sourceContentId)
                .eq("graph_version_id", graphVersionId)
                .eq("status", "DRAFT")
                .orderByDesc("opened_at")
                .last("limit 1");
        return RefinementTaskPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public RefinementTask findLatestAppliedByGraphVersionId(Long graphVersionId) {
        QueryWrapper<RefinementTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq("graph_version_id", graphVersionId)
                .eq("status", "APPLIED")
                .orderByDesc("applied_at")
                .orderByDesc("id")
                .last("limit 1");
        return RefinementTaskPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<RefinementTask> page(
            String taskType,
            String sourceContentType,
            Long sourceContentId,
            String sourceCategoryCode,
            String status,
            int pageNo,
            int pageSize) {
        QueryWrapper<RefinementTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(taskType), "task_type", taskType)
                .eq(StringUtils.isNotBlank(sourceContentType), "source_content_type", sourceContentType)
                .eq(sourceContentId != null, "source_content_id", sourceContentId)
                .eq(StringUtils.isNotBlank(sourceCategoryCode), "source_category_code", sourceCategoryCode)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .orderByDesc("opened_at")
                .orderByDesc("id");
        IPage<RefinementTaskDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                RefinementTaskPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public Long save(RefinementTask entity) {
        RefinementTaskDO dataObject = RefinementTaskPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getRefinementTaskId() == null) {
            dataObject.setRefinementTaskId(dataObject.getId());
        }
        mapper.insert(dataObject);
        return dataObject.getRefinementTaskId();
    }

    @Override
    public int update(RefinementTask entity) {
        RefinementTaskDO dataObject = RefinementTaskPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                new UpdateWrapper<RefinementTaskDO>()
                        .eq("refinement_task_id", dataObject.getRefinementTaskId())
                        .set("status", dataObject.getStatus())
                        .set("submitted_by", dataObject.getSubmittedBy())
                        .set("submitted_at", dataObject.getSubmittedAt())
                        .set("applied_by", dataObject.getAppliedBy())
                        .set("applied_at", dataObject.getAppliedAt())
                        .set("cancelled_by", dataObject.getCancelledBy())
                        .set("cancelled_at", dataObject.getCancelledAt()));
    }
}
