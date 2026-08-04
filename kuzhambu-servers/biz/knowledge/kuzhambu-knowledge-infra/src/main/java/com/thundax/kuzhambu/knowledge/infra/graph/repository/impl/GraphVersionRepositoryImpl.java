package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
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

    public GraphVersionRepositoryImpl(GraphVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphVersion findLatest(
            GraphExtractionTaskType taskType,
            String sourceContentType,
            GraphExtractionSourceContentId sourceContentId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", taskType == null ? null : taskType.value())
                .eq("source_content_type", sourceContentType)
                .eq("source_content_id", GraphExtractionSourceContentIdCodec.toValue(sourceContentId))
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
    public GraphVersion getByVersionId(GraphVersionId versionId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", GraphVersionIdCodec.toValue(versionId));
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, GraphExtractionAiCandidateId candidateId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId == null ? null : taskId.value())
                .eq("candidate_id", GraphExtractionAiCandidateIdCodec.toValue(candidateId));
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<GraphVersion> page(
            GraphExtractionTaskType taskType,
            GraphVersionStatus status,
            String sourceContentType,
            GraphExtractionSourceContentId sourceContentId,
            int pageNo,
            int pageSize) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq(taskType != null, "task_type", taskType == null ? null : taskType.value())
                .eq(status != null, "status", status == null ? null : status.value())
                .eq(StringUtils.isNotBlank(sourceContentType), "source_content_type", sourceContentType)
                .eq(
                        sourceContentId != null,
                        "source_content_id",
                        GraphExtractionSourceContentIdCodec.toValue(sourceContentId))
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
    public GraphVersionId save(GraphVersion entity) {
        GraphVersionDO dataObject = GraphVersionPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return GraphVersionIdCodec.toDomain(dataObject.getId());
    }
}
