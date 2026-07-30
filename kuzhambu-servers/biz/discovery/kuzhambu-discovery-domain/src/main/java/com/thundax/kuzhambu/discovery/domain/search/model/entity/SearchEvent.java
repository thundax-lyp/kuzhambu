package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchEvent {
    private SearchEventId id;
    private String queryText;
    private String normalizedQueryText;
    private String displayQueryText;
    private SearchIntentType intentType;
    private SearchScope searchScope;
    private Integer resultTotalCount;
    private Integer groupTotalCount;
    private Long searchLatencyMs;
    private String searchStatus;
    private String failureCode;
    private String failureMessage;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
    private Date createdAt;

    public SearchEvent(
            Long id,
            String searchEventId,
            String queryText,
            String normalizedQueryText,
            String displayQueryText,
            SearchIntentType intentType,
            SearchScope searchScope,
            Integer resultTotalCount,
            Integer groupTotalCount,
            Long searchLatencyMs,
            String searchStatus,
            String failureCode,
            String failureMessage,
            String operatorType,
            String operatorId,
            String requestId,
            String traceId,
            Date createdAt) {
        this.id = id == null ? SearchEventIdCodec.toDomain(searchEventId) : SearchEventIdCodec.toDomain(id);
        this.queryText = queryText;
        this.normalizedQueryText = normalizedQueryText;
        this.displayQueryText = displayQueryText;
        this.intentType = intentType;
        this.searchScope = searchScope;
        this.resultTotalCount = resultTotalCount;
        this.groupTotalCount = groupTotalCount;
        this.searchLatencyMs = searchLatencyMs;
        this.searchStatus = searchStatus;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.requestId = requestId;
        this.traceId = traceId;
        this.createdAt = createdAt;
    }

    public String getSearchEventId() {
        return SearchEventIdCodec.toStringValue(id);
    }

    public void setSearchEventId(String searchEventId) {
        this.id = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setId(SearchEventId id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = SearchEventIdCodec.toDomain(id);
    }
}
