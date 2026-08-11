package com.thundax.kuzhambu.operations.application.dashboard.query;

import java.time.Instant;

public record OperationsDashboardOverviewQuery(String periodType, Instant periodStart, Instant periodEnd) {}
