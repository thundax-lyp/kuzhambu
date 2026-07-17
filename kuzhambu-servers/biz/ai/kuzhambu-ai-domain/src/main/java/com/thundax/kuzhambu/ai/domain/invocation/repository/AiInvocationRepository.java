package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;

public interface AiInvocationRepository {

    AiCallRecord getCallRecord(Long callId);

    Long insertCallRecord(AiCallRecord callRecord);

    int updateCallRecord(AiCallRecord callRecord);

    List<AiCallRecord> listCallRecords(Instant requestedAtStart, Instant requestedAtEnd);

    PageResult<AiCallRecord> pageCallRecords(
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
            int pageSize);

    List<AiCallRecord> listCallRecords(
            String scope, String capability, String serviceRole, Instant requestedAtStart, Instant requestedAtEnd);

    AiCandidate getCandidate(Long candidateId);

    Long insertCandidate(AiCandidate candidate);

    int updateCandidate(AiCandidate candidate);

    List<AiCandidate> listCandidates(
            String contentType, Long contentId, Long objectId, String capability, String status);
}
