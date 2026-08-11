package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiInvocationLogIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiInvocationLogDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiInvocationRepositoryImpl implements AiInvocationRepository {

    private final AiInvocationMapper aiInvocationMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AiInvocationRepositoryImpl(AiInvocationMapper aiInvocationMapper) {
        this.aiInvocationMapper = aiInvocationMapper;
    }

    @Override
    public AiInvocationLog getByCallId(AiCallId callId) {
        return toInvocationLogDomain(aiInvocationMapper.selectOne(new LambdaQueryWrapper<AiInvocationLogDO>()
                .eq(AiInvocationLogDO::getCallId, AiCallIdCodec.toValue(callId))));
    }

    @Override
    public AiCallId insertInvocationLog(AiInvocationLog invocationLog) {
        AiInvocationLogDO dataObject = toInvocationLogObject(invocationLog);
        if (dataObject.getCallId() == null) {
            dataObject.setCallId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiInvocationMapper.insert(dataObject);
        return AiCallIdCodec.toDomain(dataObject.getCallId());
    }

    @Override
    public int updateInvocationLog(AiInvocationLog invocationLog) {
        AiInvocationLogDO dataObject = toInvocationLogObject(invocationLog);
        return aiInvocationMapper.update(
                null,
                new LambdaUpdateWrapper<AiInvocationLogDO>()
                        .eq(AiInvocationLogDO::getCallId, dataObject.getCallId())
                        .set(AiInvocationLogDO::getStatus, dataObject.getStatus())
                        .set(AiInvocationLogDO::getStreamCompleted, dataObject.getStreamCompleted())
                        .set(AiInvocationLogDO::getFallbackUsed, dataObject.getFallbackUsed())
                        .set(AiInvocationLogDO::getLatencyMs, dataObject.getLatencyMs())
                        .set(AiInvocationLogDO::getInputTokens, dataObject.getInputTokens())
                        .set(AiInvocationLogDO::getOutputTokens, dataObject.getOutputTokens())
                        .set(AiInvocationLogDO::getCostAmount, dataObject.getCostAmount())
                        .set(AiInvocationLogDO::getFailureStage, dataObject.getFailureStage())
                        .set(AiInvocationLogDO::getResultFormat, dataObject.getResultFormat())
                        .set(AiInvocationLogDO::getResultPayload, dataObject.getResultPayload())
                        .set(AiInvocationLogDO::getArtifactReferenceJson, dataObject.getArtifactReferenceJson())
                        .set(AiInvocationLogDO::getErrorType, dataObject.getErrorType())
                        .set(AiInvocationLogDO::getErrorMessage, dataObject.getErrorMessage())
                        .set(AiInvocationLogDO::getWarningsJson, dataObject.getWarningsJson())
                        .set(AiInvocationLogDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public List<AiInvocationLog> listInvocationLogs(Instant requestedAtStart, Instant requestedAtEnd) {
        return toInvocationLogDomainList(aiInvocationMapper.selectInvocationLogs(requestedAtStart, requestedAtEnd));
    }

    @Override
    public List<AiInvocationLog> listInvocationLogsByBatch(AiBatchJobId batchId) {
        return toInvocationLogDomainList(
                aiInvocationMapper.selectInvocationLogsByBatch(AiBatchJobIdCodec.toValue(batchId)));
    }

    @Override
    public List<AiInvocationLog> listInvocationLogsByBatches(List<AiBatchJobId> batchIds) {
        List<Long> values = batchIdValues(batchIds);
        if (values.isEmpty()) {
            return List.of();
        }
        return toInvocationLogDomainList(aiInvocationMapper.selectInvocationLogsByBatches(values));
    }

    @Override
    public List<AiInvocationLog> listInvocationLogsByBatchesAndContent(
            List<AiBatchJobId> batchIds, AiContentRef contentRef) {
        List<Long> values = batchIdValues(batchIds);
        if (values.isEmpty()) {
            return List.of();
        }
        return toInvocationLogDomainList(aiInvocationMapper.selectInvocationLogsByBatchesAndContent(
                values, AiContentRefCodec.toContentType(contentRef), AiContentRefCodec.toContentId(contentRef)));
    }

    @Override
    public PageResult<AiInvocationLog> page(
            String scope,
            AiBusinessCapability capability,
            AiContentRef contentRef,
            AiInvocationStatus status,
            String serviceRole,
            AiModelName modelName,
            Boolean fallbackUsed,
            Instant requestedAtStart,
            Instant requestedAtEnd,
            int pageNo,
            int pageSize) {
        PageQuery pageQuery = new PageQuery(pageNo, pageSize);
        int offset = (pageQuery.getPageNo() - 1) * pageQuery.getPageSize();
        long total = aiInvocationMapper.countInvocationLogs(
                scope,
                capabilityValue(capability),
                AiContentRefCodec.toContentType(contentRef),
                AiContentRefCodec.toContentId(contentRef),
                invocationStatusValue(status),
                serviceRole,
                AiModelNameCodec.toValue(modelName),
                fallbackUsed,
                requestedAtStart,
                requestedAtEnd);
        List<AiInvocationLogDO> records = aiInvocationMapper.selectInvocationLogsPage(
                scope,
                capabilityValue(capability),
                AiContentRefCodec.toContentType(contentRef),
                AiContentRefCodec.toContentId(contentRef),
                invocationStatusValue(status),
                serviceRole,
                AiModelNameCodec.toValue(modelName),
                fallbackUsed,
                requestedAtStart,
                requestedAtEnd,
                offset,
                pageQuery.getPageSize());
        return PageResult.of(pageQuery.getPageNo(), pageQuery.getPageSize(), total, toInvocationLogDomainList(records));
    }

    @Override
    public List<AiInvocationLog> listInvocationLogs(
            String scope,
            AiBusinessCapability capability,
            String serviceRole,
            Instant requestedAtStart,
            Instant requestedAtEnd) {
        return toInvocationLogDomainList(aiInvocationMapper.selectInvocationLogsForSummary(
                scope, capabilityValue(capability), serviceRole, requestedAtStart, requestedAtEnd));
    }

    @Override
    public AiCandidate getByCandidateId(AiCandidateId candidateId) {
        return toCandidateDomain(aiInvocationMapper.selectCandidate(AiCandidateIdCodec.toValue(candidateId)));
    }

    @Override
    public AiCandidateId insertCandidate(AiCandidate candidate) {
        AiCandidateDO dataObject = toCandidateObject(candidate);
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiInvocationMapper.insertCandidate(dataObject);
        return AiCandidateIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateCandidate(AiCandidate candidate) {
        AiCandidateDO dataObject = toCandidateObject(candidate);
        return aiInvocationMapper.updateCandidate(dataObject);
    }

    @Override
    public List<AiCandidate> listCandidates(
            AiContentRef contentRef,
            AiTargetObjectId targetObjectId,
            AiBusinessCapability capability,
            AiCandidateStatus status) {
        return toCandidateDomainList(aiInvocationMapper.selectCandidates(
                AiContentRefCodec.toContentType(contentRef),
                AiContentRefCodec.toContentId(contentRef),
                AiTargetObjectIdCodec.toValue(targetObjectId),
                capabilityValue(capability),
                candidateStatusValue(status)));
    }

    @Override
    public List<AiCandidate> listCandidatesByBatch(AiBatchJobId batchId) {
        return toCandidateDomainList(aiInvocationMapper.selectCandidatesByBatch(AiBatchJobIdCodec.toValue(batchId)));
    }

    @Override
    public List<AiCandidate> listCandidatesByBatches(List<AiBatchJobId> batchIds) {
        List<Long> values = batchIdValues(batchIds);
        if (values.isEmpty()) {
            return List.of();
        }
        return toCandidateDomainList(aiInvocationMapper.selectCandidatesByBatches(values));
    }

    @Override
    public List<AiCandidate> listCandidatesByBatchesAndContent(List<AiBatchJobId> batchIds, AiContentRef contentRef) {
        List<Long> values = batchIdValues(batchIds);
        if (values.isEmpty()) {
            return List.of();
        }
        return toCandidateDomainList(aiInvocationMapper.selectCandidatesByBatchesAndContent(
                values, AiContentRefCodec.toContentType(contentRef), AiContentRefCodec.toContentId(contentRef)));
    }

    private List<Long> batchIdValues(List<AiBatchJobId> batchIds) {
        List<Long> values = new ArrayList<>();
        if (batchIds == null) {
            return values;
        }
        for (AiBatchJobId batchId : batchIds) {
            Long value = AiBatchJobIdCodec.toValue(batchId);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private AiInvocationLogDO toInvocationLogObject(AiInvocationLog invocationLog) {
        if (invocationLog == null) {
            return null;
        }
        AiUsageSnapshot usage = AiUsageSnapshot.orEmpty(invocationLog.getUsage());
        AiInvocationLogDO dataObject = new AiInvocationLogDO();
        dataObject.setId(AiInvocationLogIdCodec.toValue(invocationLog.getId()));
        dataObject.setCallId(AiCallIdCodec.toValue(invocationLog.getCallId()));
        dataObject.setBatchId(AiBatchJobIdCodec.toValue(invocationLog.getBatchId()));
        dataObject.setScope(invocationLog.getScope());
        dataObject.setCapability(
                invocationLog.getCapability() == null
                        ? null
                        : invocationLog.getCapability().value());
        dataObject.setContentType(AiContentRefCodec.toContentType(invocationLog.getContentRef()));
        dataObject.setContentId(AiContentRefCodec.toContentId(invocationLog.getContentRef()));
        dataObject.setObjectId(AiTargetObjectIdCodec.toValue(invocationLog.getTargetObjectId()));
        dataObject.setServiceId(invocationLog.getServiceId());
        dataObject.setServiceRole(invocationLog.getServiceRole());
        dataObject.setModelId(AiModelIdCodec.toValue(invocationLog.getModelId()));
        dataObject.setModelName(AiModelNameCodec.toValue(invocationLog.getModelName()));
        dataObject.setPromptVersionId(PromptVersionIdCodec.toValue(invocationLog.getPromptVersionId()));
        dataObject.setRequestId(RequestIdCodec.toValue(invocationLog.getRequestId()));
        dataObject.setTraceId(TraceIdCodec.toValue(invocationLog.getTraceId()));
        dataObject.setStatus(
                invocationLog.getStatus() == null
                        ? null
                        : invocationLog.getStatus().name());
        dataObject.setStreamUsed(invocationLog.isStreamUsed());
        dataObject.setStreamCompleted(invocationLog.isStreamCompleted());
        dataObject.setFallbackUsed(invocationLog.isFallbackUsed());
        dataObject.setLatencyMs(usage.getLatencyMs());
        dataObject.setInputTokens(usage.getInputTokens());
        dataObject.setOutputTokens(usage.getOutputTokens());
        dataObject.setCostAmount(usage.getCostAmount());
        dataObject.setFailureStage(invocationLog.getFailureStage());
        dataObject.setResultFormat(invocationLog.getResultFormat());
        dataObject.setResultPayload(invocationLog.getResultPayload());
        dataObject.setArtifactReferenceJson(invocationLog.getArtifactReferenceJson());
        dataObject.setErrorType(invocationLog.getErrorType());
        dataObject.setErrorMessage(invocationLog.getErrorMessage());
        dataObject.setWarningsJson(invocationLog.getWarningsJson());
        dataObject.setRequestedAt(invocationLog.getRequestedAt());
        dataObject.setCompletedAt(invocationLog.getCompletedAt());
        return dataObject;
    }

    private AiInvocationLog toInvocationLogDomain(AiInvocationLogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setId(AiInvocationLogIdCodec.toDomain(dataObject.getId()));
        invocationLog.setCallId(AiCallIdCodec.toDomain(dataObject.getCallId()));
        invocationLog.setBatchId(AiBatchJobIdCodec.toDomain(dataObject.getBatchId()));
        invocationLog.setScope(dataObject.getScope());
        invocationLog.setCapability(
                dataObject.getCapability() == null ? null : AiBusinessCapability.from(dataObject.getCapability()));
        invocationLog.setContentRef(AiContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentId()));
        invocationLog.setTargetObjectId(AiTargetObjectIdCodec.toDomain(dataObject.getObjectId()));
        invocationLog.setServiceId(dataObject.getServiceId());
        invocationLog.setServiceRole(dataObject.getServiceRole());
        invocationLog.setModelId(AiModelIdCodec.toDomain(dataObject.getModelId()));
        invocationLog.setModelName(AiModelNameCodec.toDomain(dataObject.getModelName()));
        invocationLog.setPromptVersionId(PromptVersionIdCodec.toDomain(dataObject.getPromptVersionId()));
        invocationLog.setRequestId(RequestIdCodec.toDomain(dataObject.getRequestId()));
        invocationLog.setTraceId(TraceIdCodec.toDomain(dataObject.getTraceId()));
        invocationLog.setStatus(
                dataObject.getStatus() == null ? null : AiInvocationStatus.from(dataObject.getStatus()));
        invocationLog.setStreamUsed(Boolean.TRUE.equals(dataObject.getStreamUsed()));
        invocationLog.setStreamCompleted(Boolean.TRUE.equals(dataObject.getStreamCompleted()));
        invocationLog.setFallbackUsed(Boolean.TRUE.equals(dataObject.getFallbackUsed()));
        invocationLog.setUsage(new AiUsageSnapshot(
                dataObject.getLatencyMs(),
                dataObject.getInputTokens() == null ? 0 : dataObject.getInputTokens(),
                dataObject.getOutputTokens() == null ? 0 : dataObject.getOutputTokens(),
                dataObject.getCostAmount() == null ? BigDecimal.ZERO : dataObject.getCostAmount()));
        invocationLog.setFailureStage(dataObject.getFailureStage());
        invocationLog.setResultFormat(dataObject.getResultFormat());
        invocationLog.setResultPayload(dataObject.getResultPayload());
        invocationLog.setArtifactReferenceJson(dataObject.getArtifactReferenceJson());
        invocationLog.setErrorType(dataObject.getErrorType());
        invocationLog.setErrorMessage(dataObject.getErrorMessage());
        invocationLog.setWarningsJson(dataObject.getWarningsJson());
        invocationLog.setRequestedAt(dataObject.getRequestedAt());
        invocationLog.setCompletedAt(dataObject.getCompletedAt());
        return invocationLog;
    }

    private AiCandidateDO toCandidateObject(AiCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        AiCandidateDO dataObject = new AiCandidateDO();
        dataObject.setId(AiCandidateIdCodec.toValue(candidate.getId()));
        dataObject.setCallId(AiCallIdCodec.toValue(candidate.getCallId()));
        dataObject.setBatchId(AiBatchJobIdCodec.toValue(candidate.getBatchId()));
        dataObject.setCapability(
                candidate.getCapability() == null
                        ? null
                        : candidate.getCapability().value());
        dataObject.setContentType(AiContentRefCodec.toContentType(candidate.getContentRef()));
        dataObject.setContentId(AiContentRefCodec.toContentId(candidate.getContentRef()));
        dataObject.setObjectId(AiTargetObjectIdCodec.toValue(candidate.getTargetObjectId()));
        dataObject.setArtifactReferenceJson(candidate.getArtifactReferenceJson());
        dataObject.setResultFormat(candidate.getResultFormat());
        dataObject.setResultPayload(candidate.getResultPayload());
        dataObject.setStatus(
                candidate.getStatus() == null ? null : candidate.getStatus().name());
        dataObject.setPromptVersionId(AiPromptVersionIdCodec.toValue(candidate.getPromptVersionId()));
        dataObject.setModelName(AiModelNameCodec.toValue(candidate.getModelName()));
        dataObject.setFailureStage(candidate.getFailureStage());
        dataObject.setErrorType(candidate.getErrorType());
        dataObject.setErrorMessage(candidate.getErrorMessage());
        dataObject.setRequestedAt(candidate.getRequestedAt());
        dataObject.setAppliedAt(candidate.getAppliedAt());
        dataObject.setRejectedAt(candidate.getRejectedAt());
        return dataObject;
    }

    private AiCandidate toCandidateDomain(AiCandidateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(dataObject.getId()));
        candidate.setCallId(AiCallIdCodec.toDomain(dataObject.getCallId()));
        candidate.setBatchId(AiBatchJobIdCodec.toDomain(dataObject.getBatchId()));
        candidate.setCapability(
                dataObject.getCapability() == null ? null : AiBusinessCapability.from(dataObject.getCapability()));
        candidate.setContentRef(AiContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentId()));
        candidate.setTargetObjectId(AiTargetObjectIdCodec.toDomain(dataObject.getObjectId()));
        candidate.setArtifactReferenceJson(dataObject.getArtifactReferenceJson());
        candidate.setResultFormat(dataObject.getResultFormat());
        candidate.setResultPayload(dataObject.getResultPayload());
        candidate.setStatus(dataObject.getStatus() == null ? null : AiCandidateStatus.from(dataObject.getStatus()));
        candidate.setPromptVersionId(AiPromptVersionIdCodec.toDomain(dataObject.getPromptVersionId()));
        candidate.setModelName(AiModelNameCodec.toDomain(dataObject.getModelName()));
        candidate.setFailureStage(dataObject.getFailureStage());
        candidate.setErrorType(dataObject.getErrorType());
        candidate.setErrorMessage(dataObject.getErrorMessage());
        candidate.setRequestedAt(dataObject.getRequestedAt());
        candidate.setAppliedAt(dataObject.getAppliedAt());
        candidate.setRejectedAt(dataObject.getRejectedAt());
        return candidate;
    }

    private List<AiInvocationLog> toInvocationLogDomainList(List<AiInvocationLogDO> dataObjects) {
        List<AiInvocationLog> records = new ArrayList<>();
        if (dataObjects == null) {
            return records;
        }
        for (AiInvocationLogDO dataObject : dataObjects) {
            records.add(toInvocationLogDomain(dataObject));
        }
        return records;
    }

    private List<AiCandidate> toCandidateDomainList(List<AiCandidateDO> dataObjects) {
        List<AiCandidate> candidates = new ArrayList<>();
        if (dataObjects == null) {
            return candidates;
        }
        for (AiCandidateDO dataObject : dataObjects) {
            candidates.add(toCandidateDomain(dataObject));
        }
        return candidates;
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }

    private String capabilityValue(AiBusinessCapability capability) {
        return capability == null ? null : capability.value();
    }

    private String invocationStatusValue(AiInvocationStatus status) {
        return status == null ? null : status.name();
    }

    private String candidateStatusValue(AiCandidateStatus status) {
        return status == null ? null : status.name();
    }
}
