package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;

public interface AiInvocationRepository {

    AiInvocationLog getInvocationLog(AiCallId callId);

    AiCallId insertInvocationLog(AiInvocationLog invocationLog);

    int updateInvocationLog(AiInvocationLog invocationLog);

    List<AiInvocationLog> listInvocationLogs(Instant requestedAtStart, Instant requestedAtEnd);

    List<AiInvocationLog> listInvocationLogsByBatch(AiBatchJobId batchId);

    default List<AiInvocationLog> listInvocationLogsByBatches(List<AiBatchJobId> batchIds) {
        List<AiInvocationLog> records = new java.util.ArrayList<>();
        if (batchIds == null) {
            return records;
        }
        for (AiBatchJobId batchId : batchIds) {
            records.addAll(listInvocationLogsByBatch(batchId));
        }
        return records;
    }

    default List<AiInvocationLog> listInvocationLogsByBatchesAndContent(
            List<AiBatchJobId> batchIds, AiContentRef contentRef) {
        List<AiInvocationLog> records = new java.util.ArrayList<>();
        for (AiInvocationLog record : listInvocationLogsByBatches(batchIds)) {
            if (matchesContentRef(record == null ? null : record.getContentRef(), contentRef)) {
                records.add(record);
            }
        }
        return records;
    }

    PageResult<AiInvocationLog> pageInvocationLogs(
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
            int pageSize);

    List<AiInvocationLog> listInvocationLogs(
            String scope,
            AiBusinessCapability capability,
            String serviceRole,
            Instant requestedAtStart,
            Instant requestedAtEnd);

    AiCandidate getCandidate(AiCandidateId candidateId);

    AiCandidateId insertCandidate(AiCandidate candidate);

    int updateCandidate(AiCandidate candidate);

    List<AiCandidate> listCandidates(
            AiContentRef contentRef,
            AiTargetObjectId targetObjectId,
            AiBusinessCapability capability,
            AiCandidateStatus status);

    List<AiCandidate> listCandidatesByBatch(AiBatchJobId batchId);

    default List<AiCandidate> listCandidatesByBatches(List<AiBatchJobId> batchIds) {
        List<AiCandidate> records = new java.util.ArrayList<>();
        if (batchIds == null) {
            return records;
        }
        for (AiBatchJobId batchId : batchIds) {
            records.addAll(listCandidatesByBatch(batchId));
        }
        return records;
    }

    default List<AiCandidate> listCandidatesByBatchesAndContent(List<AiBatchJobId> batchIds, AiContentRef contentRef) {
        List<AiCandidate> records = new java.util.ArrayList<>();
        for (AiCandidate record : listCandidatesByBatches(batchIds)) {
            if (matchesContentRef(record == null ? null : record.getContentRef(), contentRef)) {
                records.add(record);
            }
        }
        return records;
    }

    private static boolean matchesContentRef(AiContentRef actual, AiContentRef expected) {
        String expectedType = AiContentRefCodec.toContentType(expected);
        Long expectedId = AiContentRefCodec.toContentId(expected);
        if (expectedType == null && expectedId == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        String actualType = AiContentRefCodec.toContentType(actual);
        Long actualId = AiContentRefCodec.toContentId(actual);
        return (expectedType == null || expectedType.equals(actualType))
                && (expectedId == null || expectedId.equals(actualId));
    }
}
