package com.thundax.kuzhambu.ai.application.refinement.result;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiCandidateResult {

    private final Long callId;
    private final Long candidateId;
    private final String status;
    private final String capability;
    private final String resultFormat;
    private final String resultPayload;
    private final String errorType;
    private final String errorMessage;

    public static AiCandidateResult from(AiInvokeResult result) {
        if (result == null) {
            return null;
        }
        return new AiCandidateResult(
                result.getCallId(),
                result.getCandidateId(),
                result.getStatus(),
                result.getCapability(),
                result.getResultFormat(),
                result.getResultPayload(),
                result.getErrorType(),
                result.getErrorMessage());
    }
}
