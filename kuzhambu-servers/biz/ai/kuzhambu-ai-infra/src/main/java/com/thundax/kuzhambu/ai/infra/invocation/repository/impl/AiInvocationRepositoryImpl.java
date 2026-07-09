package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCallRecordDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
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
    public AiCallRecord getCallRecord(Long callId) {
        return toCallDomain(aiInvocationMapper.selectOne(
                new LambdaQueryWrapper<AiCallRecordDO>().eq(AiCallRecordDO::getCallId, callId)));
    }

    @Override
    public Long saveCallRecord(AiCallRecord callRecord) {
        AiCallRecordDO dataObject = toCallObject(callRecord);
        if (dataObject.getCallId() == null) {
            dataObject.setCallId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiInvocationMapper.insert(dataObject);
        return dataObject.getCallId();
    }

    @Override
    public int updateCallRecord(AiCallRecord callRecord) {
        AiCallRecordDO dataObject = toCallObject(callRecord);
        return aiInvocationMapper.update(
                null,
                new LambdaUpdateWrapper<AiCallRecordDO>()
                        .eq(AiCallRecordDO::getCallId, dataObject.getCallId())
                        .set(AiCallRecordDO::getStatus, dataObject.getStatus())
                        .set(AiCallRecordDO::getStreamCompleted, dataObject.getStreamCompleted())
                        .set(AiCallRecordDO::getFallbackUsed, dataObject.getFallbackUsed())
                        .set(AiCallRecordDO::getLatencyMs, dataObject.getLatencyMs())
                        .set(AiCallRecordDO::getInputTokens, dataObject.getInputTokens())
                        .set(AiCallRecordDO::getOutputTokens, dataObject.getOutputTokens())
                        .set(AiCallRecordDO::getCostAmount, dataObject.getCostAmount())
                        .set(AiCallRecordDO::getFailureStage, dataObject.getFailureStage())
                        .set(AiCallRecordDO::getResultFormat, dataObject.getResultFormat())
                        .set(AiCallRecordDO::getResultPayload, dataObject.getResultPayload())
                        .set(AiCallRecordDO::getArtifactReferenceJson, dataObject.getArtifactReferenceJson())
                        .set(AiCallRecordDO::getErrorType, dataObject.getErrorType())
                        .set(AiCallRecordDO::getErrorMessage, dataObject.getErrorMessage())
                        .set(AiCallRecordDO::getWarningsJson, dataObject.getWarningsJson())
                        .set(AiCallRecordDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public List<AiCallRecord> listCallRecords(Instant requestedAtStart, Instant requestedAtEnd) {
        return toCallDomainList(aiInvocationMapper.selectCallRecords(requestedAtStart, requestedAtEnd));
    }

    @Override
    public PageResult<AiCallRecord> pageCallRecords(
            String scope,
            String capability,
            String contentType,
            Long contentId,
            String status,
            String serviceRole,
            String modelName,
            Boolean fallbackUsed,
            Instant requestedAtStart,
            Instant requestedAtEnd,
            int pageNo,
            int pageSize) {
        PageQuery pageQuery = new PageQuery(pageNo, pageSize);
        int offset = (pageQuery.getPageNo() - 1) * pageQuery.getPageSize();
        long total = aiInvocationMapper.countCallRecords(
                scope,
                capability,
                contentType,
                contentId,
                status,
                serviceRole,
                modelName,
                fallbackUsed,
                requestedAtStart,
                requestedAtEnd);
        List<AiCallRecordDO> records = aiInvocationMapper.selectCallRecordsPage(
                scope,
                capability,
                contentType,
                contentId,
                status,
                serviceRole,
                modelName,
                fallbackUsed,
                requestedAtStart,
                requestedAtEnd,
                offset,
                pageQuery.getPageSize());
        return PageResult.of(pageQuery.getPageNo(), pageQuery.getPageSize(), total, toCallDomainList(records));
    }

    @Override
    public List<AiCallRecord> listCallRecords(
            String scope, String capability, String serviceRole, Instant requestedAtStart, Instant requestedAtEnd) {
        return toCallDomainList(aiInvocationMapper.selectCallRecordsForSummary(
                scope, capability, serviceRole, requestedAtStart, requestedAtEnd));
    }

    @Override
    public AiCandidate getCandidate(Long candidateId) {
        return toCandidateDomain(aiInvocationMapper.selectCandidate(candidateId));
    }

    @Override
    public Long saveCandidate(AiCandidate candidate) {
        AiCandidateDO dataObject = toCandidateObject(candidate);
        if (dataObject.getCandidateId() == null) {
            dataObject.setCandidateId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiInvocationMapper.insertCandidate(dataObject);
        return dataObject.getCandidateId();
    }

    @Override
    public int updateCandidate(AiCandidate candidate) {
        AiCandidateDO dataObject = toCandidateObject(candidate);
        return aiInvocationMapper.updateCandidate(dataObject);
    }

    @Override
    public List<AiCandidate> listCandidates(
            String contentType, Long contentId, Long objectId, String capability, String status) {
        return toCandidateDomainList(
                aiInvocationMapper.selectCandidates(contentType, contentId, objectId, capability, status));
    }

    private AiCallRecordDO toCallObject(AiCallRecord record) {
        if (record == null) {
            return null;
        }
        AiUsageSnapshot usage = AiUsageSnapshot.orEmpty(record.getUsage());
        AiCallRecordDO dataObject = new AiCallRecordDO();
        dataObject.setId(record.getId());
        dataObject.setCallId(record.getCallId());
        dataObject.setBatchId(record.getBatchId());
        dataObject.setScope(record.getScope());
        dataObject.setCapability(record.getCapability());
        dataObject.setContentType(record.getContentType());
        dataObject.setContentId(record.getContentId());
        dataObject.setObjectId(record.getObjectId());
        dataObject.setServiceId(record.getServiceId());
        dataObject.setServiceRole(record.getServiceRole());
        dataObject.setModelId(record.getModelId());
        dataObject.setModelName(record.getModelName());
        dataObject.setPromptVersionId(record.getPromptVersionId());
        dataObject.setRequestId(record.getRequestId());
        dataObject.setTraceId(record.getTraceId());
        dataObject.setStatus(record.getStatus());
        dataObject.setStreamUsed(record.isStreamUsed());
        dataObject.setStreamCompleted(record.isStreamCompleted());
        dataObject.setFallbackUsed(record.isFallbackUsed());
        dataObject.setLatencyMs(usage.getLatencyMs());
        dataObject.setInputTokens(usage.getInputTokens());
        dataObject.setOutputTokens(usage.getOutputTokens());
        dataObject.setCostAmount(usage.getCostAmount());
        dataObject.setFailureStage(record.getFailureStage());
        dataObject.setResultFormat(record.getResultFormat());
        dataObject.setResultPayload(record.getResultPayload());
        dataObject.setArtifactReferenceJson(record.getArtifactReferenceJson());
        dataObject.setErrorType(record.getErrorType());
        dataObject.setErrorMessage(record.getErrorMessage());
        dataObject.setWarningsJson(record.getWarningsJson());
        dataObject.setRequestedAt(record.getRequestedAt());
        dataObject.setCompletedAt(record.getCompletedAt());
        return dataObject;
    }

    private AiCallRecord toCallDomain(AiCallRecordDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AiCallRecord record = new AiCallRecord();
        record.setId(dataObject.getId());
        record.setCallId(dataObject.getCallId());
        record.setBatchId(dataObject.getBatchId());
        record.setScope(dataObject.getScope());
        record.setCapability(dataObject.getCapability());
        record.setContentType(dataObject.getContentType());
        record.setContentId(dataObject.getContentId());
        record.setObjectId(dataObject.getObjectId());
        record.setServiceId(dataObject.getServiceId());
        record.setServiceRole(dataObject.getServiceRole());
        record.setModelId(dataObject.getModelId());
        record.setModelName(dataObject.getModelName());
        record.setPromptVersionId(dataObject.getPromptVersionId());
        record.setRequestId(dataObject.getRequestId());
        record.setTraceId(dataObject.getTraceId());
        record.setStatus(dataObject.getStatus());
        record.setStreamUsed(Boolean.TRUE.equals(dataObject.getStreamUsed()));
        record.setStreamCompleted(Boolean.TRUE.equals(dataObject.getStreamCompleted()));
        record.setFallbackUsed(Boolean.TRUE.equals(dataObject.getFallbackUsed()));
        record.setUsage(new AiUsageSnapshot(
                dataObject.getLatencyMs(),
                dataObject.getInputTokens() == null ? 0 : dataObject.getInputTokens(),
                dataObject.getOutputTokens() == null ? 0 : dataObject.getOutputTokens(),
                dataObject.getCostAmount() == null ? BigDecimal.ZERO : dataObject.getCostAmount()));
        record.setFailureStage(dataObject.getFailureStage());
        record.setResultFormat(dataObject.getResultFormat());
        record.setResultPayload(dataObject.getResultPayload());
        record.setArtifactReferenceJson(dataObject.getArtifactReferenceJson());
        record.setErrorType(dataObject.getErrorType());
        record.setErrorMessage(dataObject.getErrorMessage());
        record.setWarningsJson(dataObject.getWarningsJson());
        record.setRequestedAt(dataObject.getRequestedAt());
        record.setCompletedAt(dataObject.getCompletedAt());
        return record;
    }

    private AiCandidateDO toCandidateObject(AiCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        AiCandidateDO dataObject = new AiCandidateDO();
        dataObject.setId(candidate.getId());
        dataObject.setCandidateId(candidate.getCandidateId());
        dataObject.setCallId(candidate.getCallId());
        dataObject.setBatchId(candidate.getBatchId());
        dataObject.setCapability(candidate.getCapability());
        dataObject.setContentType(candidate.getContentType());
        dataObject.setContentId(candidate.getContentId());
        dataObject.setObjectId(candidate.getObjectId());
        dataObject.setArtifactReferenceJson(candidate.getArtifactReferenceJson());
        dataObject.setResultFormat(candidate.getResultFormat());
        dataObject.setResultPayload(candidate.getResultPayload());
        dataObject.setStatus(candidate.getStatus());
        dataObject.setPromptVersionId(candidate.getPromptVersionId());
        dataObject.setModelName(candidate.getModelName());
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
        candidate.setId(dataObject.getId());
        candidate.setCandidateId(dataObject.getCandidateId());
        candidate.setCallId(dataObject.getCallId());
        candidate.setBatchId(dataObject.getBatchId());
        candidate.setCapability(dataObject.getCapability());
        candidate.setContentType(dataObject.getContentType());
        candidate.setContentId(dataObject.getContentId());
        candidate.setObjectId(dataObject.getObjectId());
        candidate.setArtifactReferenceJson(dataObject.getArtifactReferenceJson());
        candidate.setResultFormat(dataObject.getResultFormat());
        candidate.setResultPayload(dataObject.getResultPayload());
        candidate.setStatus(dataObject.getStatus());
        candidate.setPromptVersionId(dataObject.getPromptVersionId());
        candidate.setModelName(dataObject.getModelName());
        candidate.setFailureStage(dataObject.getFailureStage());
        candidate.setErrorType(dataObject.getErrorType());
        candidate.setErrorMessage(dataObject.getErrorMessage());
        candidate.setRequestedAt(dataObject.getRequestedAt());
        candidate.setAppliedAt(dataObject.getAppliedAt());
        candidate.setRejectedAt(dataObject.getRejectedAt());
        return candidate;
    }

    private List<AiCallRecord> toCallDomainList(List<AiCallRecordDO> dataObjects) {
        List<AiCallRecord> records = new ArrayList<>();
        if (dataObjects == null) {
            return records;
        }
        for (AiCallRecordDO dataObject : dataObjects) {
            records.add(toCallDomain(dataObject));
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
}
