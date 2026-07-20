package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
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
    private Long id;
    private String searchEventId;
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
}
