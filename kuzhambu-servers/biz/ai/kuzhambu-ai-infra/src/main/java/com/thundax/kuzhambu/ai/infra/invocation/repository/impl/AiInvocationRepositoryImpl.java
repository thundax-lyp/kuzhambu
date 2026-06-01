package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper.AiCallRecordDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper.AiCandidateDO;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
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
                        .set(AiCallRecordDO::getErrorType, dataObject.getErrorType())
                        .set(AiCallRecordDO::getErrorMessage, dataObject.getErrorMessage())
                        .set(AiCallRecordDO::getWarningsJson, dataObject.getWarningsJson())
                        .set(AiCallRecordDO::getCompletedAt, dataObject.getCompletedAt()));
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
    public List<AiCandidate> listCandidates(String contentType, Long contentId, String capability, String status) {
        return toCandidateDomainList(aiInvocationMapper.selectCandidates(contentType, contentId, capability, status));
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
        return new AiCallRecord(
                dataObject.getId(),
                dataObject.getCallId(),
                dataObject.getBatchId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getContentType(),
                dataObject.getContentId(),
                dataObject.getObjectId(),
                dataObject.getServiceId(),
                dataObject.getServiceRole(),
                dataObject.getModelId(),
                dataObject.getModelName(),
                dataObject.getPromptVersionId(),
                dataObject.getRequestId(),
                dataObject.getTraceId(),
                dataObject.getStatus(),
                Boolean.TRUE.equals(dataObject.getStreamUsed()),
                Boolean.TRUE.equals(dataObject.getStreamCompleted()),
                Boolean.TRUE.equals(dataObject.getFallbackUsed()),
                new AiUsageSnapshot(
                        dataObject.getLatencyMs(),
                        dataObject.getInputTokens() == null ? 0 : dataObject.getInputTokens(),
                        dataObject.getOutputTokens() == null ? 0 : dataObject.getOutputTokens(),
                        dataObject.getCostAmount() == null ? BigDecimal.ZERO : dataObject.getCostAmount()),
                dataObject.getErrorType(),
                dataObject.getErrorMessage(),
                dataObject.getWarningsJson(),
                dataObject.getRequestedAt(),
                dataObject.getCompletedAt());
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
        dataObject.setResultFormat(candidate.getResultFormat());
        dataObject.setResultPayload(candidate.getResultPayload());
        dataObject.setStatus(candidate.getStatus());
        dataObject.setPromptVersionId(candidate.getPromptVersionId());
        dataObject.setModelName(candidate.getModelName());
        dataObject.setErrorType(candidate.getErrorType());
        dataObject.setErrorMessage(candidate.getErrorMessage());
        dataObject.setRequestedAt(candidate.getRequestedAt());
        dataObject.setAppliedAt(candidate.getAppliedAt());
        return dataObject;
    }

    private AiCandidate toCandidateDomain(AiCandidateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiCandidate(
                dataObject.getId(),
                dataObject.getCandidateId(),
                dataObject.getCallId(),
                dataObject.getBatchId(),
                dataObject.getCapability(),
                dataObject.getContentType(),
                dataObject.getContentId(),
                dataObject.getObjectId(),
                dataObject.getResultFormat(),
                dataObject.getResultPayload(),
                dataObject.getStatus(),
                dataObject.getPromptVersionId(),
                dataObject.getModelName(),
                dataObject.getErrorType(),
                dataObject.getErrorMessage(),
                dataObject.getRequestedAt(),
                dataObject.getAppliedAt());
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
