package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryUnderstanding {
    private Long id;
    private String queryUnderstandingId;
    private String searchEventId;
    private String queryText;
    private String normalizedQueryText;
    private String rewrittenQueryText;
    private SearchIntentType intentType;
    private String recognizedEntitiesJson;
    private String expandedSynonymsJson;
    private String understandingStatus;
    private String failureCode;
    private String failureMessage;
    private String requestId;
    private String traceId;
    private Date createdAt;
}
