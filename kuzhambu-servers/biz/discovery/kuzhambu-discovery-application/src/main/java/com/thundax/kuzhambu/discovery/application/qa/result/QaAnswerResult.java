package com.thundax.kuzhambu.discovery.application.qa.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaAnswerResult {
    private Long sessionId;
    private Long questionMessageId;
    private Long answerMessageId;
    private String question;
    private String answer;
    private String answerStatus;
    private String failureReason;
    private List<QaSourceResult> sources;
    private TraceSummaryResult traceSummary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceSummaryResult {
        private Long traceId;
        private String rewrittenQuestion;
        private Integer candidateCount;
        private String expandedTermsJson;
        private String linkedEntitiesJson;
    }
}
