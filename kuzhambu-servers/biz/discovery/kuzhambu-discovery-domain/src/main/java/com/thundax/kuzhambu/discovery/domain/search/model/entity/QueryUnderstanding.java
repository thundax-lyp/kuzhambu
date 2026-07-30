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
    private Long searchEventId;
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

    public QueryUnderstanding(
            Long id,
            String queryUnderstandingId,
            String searchEventId,
            String queryText,
            String normalizedQueryText,
            String rewrittenQueryText,
            SearchIntentType intentType,
            String recognizedEntitiesJson,
            String expandedSynonymsJson,
            String understandingStatus,
            String failureCode,
            String failureMessage,
            String requestId,
            String traceId,
            Date createdAt) {
        this.id = id == null ? parseId(queryUnderstandingId) : id;
        this.searchEventId = parseId(searchEventId);
        this.queryText = queryText;
        this.normalizedQueryText = normalizedQueryText;
        this.rewrittenQueryText = rewrittenQueryText;
        this.intentType = intentType;
        this.recognizedEntitiesJson = recognizedEntitiesJson;
        this.expandedSynonymsJson = expandedSynonymsJson;
        this.understandingStatus = understandingStatus;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.requestId = requestId;
        this.traceId = traceId;
        this.createdAt = createdAt;
    }

    public String getQueryUnderstandingId() {
        return id == null ? null : String.valueOf(id);
    }

    public void setQueryUnderstandingId(String queryUnderstandingId) {
        this.id = parseId(queryUnderstandingId);
    }

    public void setSearchEventId(String searchEventId) {
        this.searchEventId = parseId(searchEventId);
    }

    public void setSearchEventId(Long searchEventId) {
        this.searchEventId = searchEventId;
    }

    private Long parseId(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
