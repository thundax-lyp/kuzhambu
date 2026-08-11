package com.thundax.kuzhambu.discovery.application.search.query;

import java.time.Instant;
import java.util.List;

public record SearchEventQuery(
        Long eventId,
        String queryText,
        List<String> intentTypes,
        List<String> searchStatuses,
        String operatorId,
        Instant dateFrom,
        Instant dateTo) {}
