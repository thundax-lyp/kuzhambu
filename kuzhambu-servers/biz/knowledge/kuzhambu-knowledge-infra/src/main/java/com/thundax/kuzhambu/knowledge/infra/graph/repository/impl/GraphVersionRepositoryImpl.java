package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphVersionPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphVersionMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class GraphVersionRepositoryImpl implements GraphVersionRepository {

    private final GraphVersionMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public GraphVersionRepositoryImpl(GraphVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", taskType)
                .eq("source_content_type", sourceContentType)
                .eq("source_content_id", sourceContentId)
                .orderByDesc("version_no")
                .last("limit 1");
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public GraphVersion findLatestAppliedByCategoryCode(String sourceCategoryCode) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPLIED")
                .eq("source_category_code", sourceCategoryCode)
                .orderByDesc("applied_at")
                .orderByDesc("version_no")
                .orderByDesc("id")
                .last("limit 1");
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public List<GraphVersion> listAppliedByCategoryCode(String sourceCategoryCode) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPLIED")
                .eq(StringUtils.isNotBlank(sourceCategoryCode), "source_category_code", sourceCategoryCode)
                .orderByDesc("applied_at")
                .orderByDesc("version_no")
                .orderByDesc("id");
        return mapper.selectList(wrapper).stream()
                .map(GraphVersionPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphVersion getByVersionId(Long versionId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("version_id", versionId);
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId == null ? null : taskId.value()).eq("candidate_id", candidateId);
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<GraphVersion> page(
            String taskType, String status, String sourceContentType, Long sourceContentId, int pageNo, int pageSize) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(taskType), "task_type", taskType)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .eq(StringUtils.isNotBlank(sourceContentType), "source_content_type", sourceContentType)
                .eq(sourceContentId != null, "source_content_id", sourceContentId)
                .orderByDesc("applied_at")
                .orderByDesc("version_no")
                .orderByDesc("id");
        IPage<GraphVersionDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                dataObjectPage.getRecords().stream()
                        .map(GraphVersionPersistenceAssembler::toDomain)
                        .toList());
    }

    @Override
    public Long save(GraphVersion entity) {
        GraphVersionDO dataObject = GraphVersionPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getVersionId() == null) {
            dataObject.setVersionId(dataObject.getId());
        }
        mapper.insert(dataObject);
        return dataObject.getVersionId();
    }
}
