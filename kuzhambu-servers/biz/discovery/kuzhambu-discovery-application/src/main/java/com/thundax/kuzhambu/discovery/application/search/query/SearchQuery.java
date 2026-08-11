package com.thundax.kuzhambu.discovery.application.search.query;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.time.Instant;
import java.util.List;

public record SearchQuery(
        String queryText,
        List<String> knowledgeBases,
        List<String> categoryCodes,
        List<String> tagNames,
        Instant dateFrom,
        Instant dateTo,
        String operatorType,
        String operatorId,
        RequestId requestId,
        TraceId traceId) {}
