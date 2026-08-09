package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import java.time.Instant;

public record AiReportSummaryQuery(Instant periodStart, Instant periodEnd, AiReportBucketType bucketType) {}
