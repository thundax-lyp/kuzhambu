package com.thundax.kuzhambu.discovery.application.search.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchLogResult {
    private String searchLogId;
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
}
