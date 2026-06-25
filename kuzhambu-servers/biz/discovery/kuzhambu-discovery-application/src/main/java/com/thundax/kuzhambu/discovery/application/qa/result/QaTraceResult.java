package com.thundax.kuzhambu.discovery.application.qa.result;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaTraceResult {
    private Long traceId;
    private Long messageId;
    private String rawQuestion;
    private String rewrittenQuestion;
    private String scope;
    private String filtersJson;
    private String expandedTermsJson;
    private String linkedEntitiesJson;
    private Integer candidateCount;
    private String contextSnapshot;
    private Date retrievedAt;
}
