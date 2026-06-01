package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import java.util.List;

public interface AiInvocationRepository {

    AiCallRecord getCallRecord(Long callId);

    Long saveCallRecord(AiCallRecord callRecord);

    int updateCallRecord(AiCallRecord callRecord);

    AiCandidate getCandidate(Long candidateId);

    Long saveCandidate(AiCandidate candidate);

    int updateCandidate(AiCandidate candidate);

    List<AiCandidate> listCandidates(String contentType, Long contentId, String capability, String status);
}
