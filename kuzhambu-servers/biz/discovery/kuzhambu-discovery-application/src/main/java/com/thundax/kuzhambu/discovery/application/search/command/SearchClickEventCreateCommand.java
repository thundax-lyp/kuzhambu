package com.thundax.kuzhambu.discovery.application.search.command;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;

public record SearchClickEventCreateCommand(
        SearchEventId searchEventId,
        String contentDomain,
        String contentType,
        String contentId,
        String contentTitle,
        String resultGroupKey,
        int resultRank,
        int groupRank,
        String targetPath,
        String operatorType,
        String operatorId,
        RequestId requestId,
        TraceId traceId) {}
