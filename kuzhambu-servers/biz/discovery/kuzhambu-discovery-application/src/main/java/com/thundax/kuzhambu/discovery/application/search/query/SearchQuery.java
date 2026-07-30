package com.thundax.kuzhambu.discovery.application.search.query;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchQuery {
    private String queryText;
    private List<String> knowledgeBases;
    private List<String> categoryCodes;
    private List<String> tagNames;
    private List<String> contentStatuses;
    private List<String> visibilityScopes;
    private Date dateFrom;
    private Date dateTo;
    private int pageNo;
    private int pageSize;
    private String operatorType;
    private String operatorId;
    private RequestId requestId;
    private TraceId traceId;
}
