package com.thundax.kuzhambu.classics.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClassicsBatchOperationItemResult {
    private final String contentType;
    private final Long contentId;
    private final Long resultId;
    private final String status;
    private final String failureCode;
    private final String failureReason;

    public static ClassicsBatchOperationItemResult success(
            String contentType, Long contentId, Long resultId, String status) {
        return ClassicsBatchOperationItemResult.builder()
                .contentType(contentType)
                .contentId(contentId)
                .resultId(resultId)
                .status(status)
                .build();
    }

    public static ClassicsBatchOperationItemResult failure(
            String contentType, Long contentId, String failureCode, String failureReason) {
        return ClassicsBatchOperationItemResult.builder()
                .contentType(contentType)
                .contentId(contentId)
                .failureCode(failureCode)
                .failureReason(failureReason)
                .build();
    }
}
