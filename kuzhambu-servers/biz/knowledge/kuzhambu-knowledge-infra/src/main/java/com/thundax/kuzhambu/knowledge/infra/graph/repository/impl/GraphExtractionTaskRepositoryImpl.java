package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionBatchJobIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionBatchJobId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeGraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class GraphExtractionTaskRepositoryImpl implements GraphExtractionTaskRepository {

    private final GraphExtractionTaskMapper mapper;

    public GraphExtractionTaskRepositoryImpl(GraphExtractionTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId) {
        QueryWrapper<GraphExtractionTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", valueOf(taskId));
        return KnowledgeGraphPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public GraphExtractionTaskId save(GraphExtractionTask entity) {
        GraphExtractionTaskDO dataObject = KnowledgeGraphPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return GraphExtractionTaskIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(GraphExtractionTask entity) {
        GraphExtractionTaskDO dataObject = KnowledgeGraphPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<GraphExtractionTaskDO>()
                        .eq(GraphExtractionTaskDO::getId, dataObject.getId())
                        .set(GraphExtractionTaskDO::getBatchJobId, dataObject.getBatchJobId())
                        .set(GraphExtractionTaskDO::getTaskType, dataObject.getTaskType())
                        .set(GraphExtractionTaskDO::getScopeType, dataObject.getScopeType())
                        .set(GraphExtractionTaskDO::getScopeJson, dataObject.getScopeJson())
                        .set(GraphExtractionTaskDO::getTriggerSource, dataObject.getTriggerSource())
                        .set(GraphExtractionTaskDO::getSelectionScopeJson, dataObject.getSelectionScopeJson())
                        .set(GraphExtractionTaskDO::getReplaceUnconfirmedOnly, dataObject.getReplaceUnconfirmedOnly())
                        .set(GraphExtractionTaskDO::getParentTaskId, dataObject.getParentTaskId())
                        .set(GraphExtractionTaskDO::getSourceContentType, dataObject.getSourceContentType())
                        .set(GraphExtractionTaskDO::getSourceContentId, dataObject.getSourceContentId())
                        .set(GraphExtractionTaskDO::getModelId, dataObject.getModelId())
                        .set(GraphExtractionTaskDO::getModelName, dataObject.getModelName())
                        .set(GraphExtractionTaskDO::getPromptVersionId, dataObject.getPromptVersionId())
                        .set(GraphExtractionTaskDO::getRequestId, dataObject.getRequestId())
                        .set(GraphExtractionTaskDO::getTraceId, dataObject.getTraceId())
                        .set(GraphExtractionTaskDO::getPromptMessagesJson, dataObject.getPromptMessagesJson())
                        .set(GraphExtractionTaskDO::getPromptVariablesJson, dataObject.getPromptVariablesJson())
                        .set(GraphExtractionTaskDO::getPromptHash, dataObject.getPromptHash())
                        .set(GraphExtractionTaskDO::getInputPayloadJson, dataObject.getInputPayloadJson())
                        .set(GraphExtractionTaskDO::getOutputSchemaJson, dataObject.getOutputSchemaJson())
                        .set(GraphExtractionTaskDO::getForceJson, dataObject.getForceJson())
                        .set(GraphExtractionTaskDO::getLocale, dataObject.getLocale())
                        .set(GraphExtractionTaskDO::getAiCallId, dataObject.getAiCallId())
                        .set(GraphExtractionTaskDO::getAiCandidateId, dataObject.getAiCandidateId())
                        .set(GraphExtractionTaskDO::getStatus, dataObject.getStatus())
                        .set(GraphExtractionTaskDO::getErrorType, dataObject.getErrorType())
                        .set(GraphExtractionTaskDO::getErrorMessage, dataObject.getErrorMessage())
                        .set(GraphExtractionTaskDO::getRequestedBy, dataObject.getRequestedBy())
                        .set(GraphExtractionTaskDO::getRequestedAt, dataObject.getRequestedAt())
                        .set(GraphExtractionTaskDO::getCompletedAt, dataObject.getCompletedAt())
                        .set(GraphExtractionTaskDO::getAppliedAt, dataObject.getAppliedAt()));
    }

    @Override
    public List<GraphExtractionTask> listByBatchJobId(GraphExtractionBatchJobId batchJobId) {
        Long batchJobIdValue = GraphExtractionBatchJobIdCodec.toValue(batchJobId);
        QueryWrapper<GraphExtractionTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq(batchJobIdValue != null, "batch_job_id", batchJobIdValue)
                .orderByAsc("requested_at")
                .orderByAsc("id");
        return KnowledgeGraphPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public PageResult<GraphExtractionTask> page(
            String taskType,
            GraphExtractionBatchJobId batchJobId,
            String triggerSource,
            String status,
            String sourceContentType,
            GraphExtractionSourceContentId sourceContentId,
            int pageNo,
            int pageSize) {
        Long batchJobIdValue = GraphExtractionBatchJobIdCodec.toValue(batchJobId);
        Long sourceContentIdValue = GraphExtractionSourceContentIdCodec.toValue(sourceContentId);
        QueryWrapper<GraphExtractionTaskDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(taskType), "task_type", taskType)
                .eq(batchJobIdValue != null, "batch_job_id", batchJobIdValue)
                .eq(StringUtils.isNotBlank(triggerSource), "trigger_source", triggerSource)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .eq(StringUtils.isNotBlank(sourceContentType), "source_content_type", sourceContentType)
                .eq(sourceContentIdValue != null, "source_content_id", sourceContentIdValue)
                .orderByDesc("requested_at")
                .orderByDesc("id");
        IPage<GraphExtractionTaskDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                KnowledgeGraphPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    private Long valueOf(GraphExtractionTaskId id) {
        return id == null ? null : id.value();
    }
}
