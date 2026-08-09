package com.thundax.kuzhambu.discovery.application.search.query;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.time.Instant;
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
    private Instant dateFrom;
    private Instant dateTo;
    private String operatorType;
    private String operatorId;
    private RequestId requestId;
    private TraceId traceId;
}
