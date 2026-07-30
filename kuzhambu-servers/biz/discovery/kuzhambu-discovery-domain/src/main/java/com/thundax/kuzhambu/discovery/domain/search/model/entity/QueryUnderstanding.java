package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import com.thundax.kuzhambu.discovery.domain.search.codec.QueryUnderstandingIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.QueryUnderstandingId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
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
    private QueryUnderstandingId id;
    private SearchEventId searchEventId;
    private String queryText;
    private String normalizedQueryText;
    private String rewrittenQueryText;
    private SearchIntentType intentType;
    private String recognizedEntitiesJson;
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
            String understandingStatus,
            String failureCode,
            String failureMessage,
            String requestId,
            String traceId,
            Date createdAt) {
        this.id = id == null
                ? QueryUnderstandingIdCodec.toDomain(queryUnderstandingId)
                : QueryUnderstandingIdCodec.toDomain(id);
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
        this.queryText = queryText;
        this.normalizedQueryText = normalizedQueryText;
        this.rewrittenQueryText = rewrittenQueryText;
        this.intentType = intentType;
        this.recognizedEntitiesJson = recognizedEntitiesJson;
        this.understandingStatus = understandingStatus;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.requestId = requestId;
        this.traceId = traceId;
        this.createdAt = createdAt;
    }

    public String getQueryUnderstandingId() {
        return QueryUnderstandingIdCodec.toStringValue(id);
    }

    public void setQueryUnderstandingId(String queryUnderstandingId) {
        this.id = QueryUnderstandingIdCodec.toDomain(queryUnderstandingId);
    }

    public void setId(QueryUnderstandingId id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = QueryUnderstandingIdCodec.toDomain(id);
    }

    public void setSearchEventId(String searchEventId) {
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setSearchEventId(Long searchEventId) {
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setSearchEventId(SearchEventId searchEventId) {
        this.searchEventId = searchEventId;
    }
}
