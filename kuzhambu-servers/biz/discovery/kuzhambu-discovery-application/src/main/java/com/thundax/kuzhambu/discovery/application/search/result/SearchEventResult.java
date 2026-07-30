package com.thundax.kuzhambu.discovery.application.search.result;

import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchEventResult {
    private SearchEventId id;
    private String queryText;
    private String normalizedQueryText;
    private String displayQueryText;
    private String intentType;
    private String searchScopesJson;
    private int resultTotalCount;
    private int groupTotalCount;
    private String searchStatus;
    private String failureCode;
    private String failureMessage;
    private String operatorId;
    private String requestId;
    private String traceId;
    private Long createdAt;
    private List<SearchGroupResult> groups;

    public SearchEventResult(
            String searchEventId,
            String queryText,
            String normalizedQueryText,
            String displayQueryText,
            String intentType,
            String searchScopesJson,
            int resultTotalCount,
            int groupTotalCount,
            String searchStatus,
            String failureCode,
            String failureMessage,
            String operatorId,
            String requestId,
            String traceId,
            Long createdAt,
            List<SearchGroupResult> groups) {
        this(
                SearchEventIdCodec.toDomain(searchEventId),
                queryText,
                normalizedQueryText,
                displayQueryText,
                intentType,
                searchScopesJson,
                resultTotalCount,
                groupTotalCount,
                searchStatus,
                failureCode,
                failureMessage,
                operatorId,
                requestId,
                traceId,
                createdAt,
                groups);
    }

    public String getSearchEventId() {
        return SearchEventIdCodec.toStringValue(id);
    }

    public void setSearchEventId(String searchEventId) {
        this.id = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setId(Long id) {
        this.id = SearchEventIdCodec.toDomain(id);
    }
}
