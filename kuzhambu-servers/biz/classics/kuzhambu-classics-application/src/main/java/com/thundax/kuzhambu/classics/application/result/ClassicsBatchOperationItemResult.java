package com.thundax.kuzhambu.classics.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClassicsBatchOperationItemResult {
    private final Long candidateId;
    private final Long objectId;
    private final String capability;
    private final String contentType;
    private final Long contentId;
    private final Long resultId;
    private final String status;
    private final String failureCode;
    private final String failureReason;

    public static ClassicsBatchOperationItemResult success(
            String contentType, Long contentId, Long resultId, String status) {
        return ClassicsBatchOperationItemResult.builder()
                .candidateId(null)
                .objectId(null)
                .capability(null)
                .contentType(contentType)
                .contentId(contentId)
                .resultId(resultId)
                .status(status)
                .build();
    }

    public static ClassicsBatchOperationItemResult failure(
            String contentType, Long contentId, String failureCode, String failureReason) {
        return ClassicsBatchOperationItemResult.builder()
                .candidateId(null)
                .objectId(null)
                .capability(null)
                .contentType(contentType)
                .contentId(contentId)
                .failureCode(failureCode)
                .failureReason(failureReason)
                .build();
    }

    public static ClassicsBatchOperationItemResult successForCandidate(
            String contentType,
            Long contentId,
            Long resultId,
            String status,
            Long candidateId,
            Long objectId,
            String capability) {
        return ClassicsBatchOperationItemResult.builder()
                .candidateId(candidateId)
                .objectId(objectId)
                .capability(capability)
                .contentType(contentType)
                .contentId(contentId)
                .resultId(resultId)
                .status(status)
                .build();
    }

    public static ClassicsBatchOperationItemResult failureForCandidate(
            String contentType,
            Long contentId,
            String failureCode,
            String failureReason,
            Long candidateId,
            Long objectId,
            String capability) {
        return ClassicsBatchOperationItemResult.builder()
                .candidateId(candidateId)
                .objectId(objectId)
                .capability(capability)
                .contentType(contentType)
                .contentId(contentId)
                .failureCode(failureCode)
                .failureReason(failureReason)
                .build();
    }
}
