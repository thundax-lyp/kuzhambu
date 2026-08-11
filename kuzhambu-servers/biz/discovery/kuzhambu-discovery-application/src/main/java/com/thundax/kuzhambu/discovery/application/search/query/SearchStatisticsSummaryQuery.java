package com.thundax.kuzhambu.discovery.application.search.query;

import java.time.Instant;

public record SearchStatisticsSummaryQuery(Instant dateFrom, Instant dateTo) {}
