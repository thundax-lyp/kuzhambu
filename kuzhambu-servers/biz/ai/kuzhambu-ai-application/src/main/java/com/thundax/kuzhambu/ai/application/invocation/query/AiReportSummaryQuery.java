package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import java.time.Instant;

public class AiReportSummaryQuery {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final AiReportBucketType bucketType;

    public AiReportSummaryQuery(Instant periodStart, Instant periodEnd, AiReportBucketType bucketType) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.bucketType = bucketType;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public AiReportBucketType getBucketType() {
        return bucketType;
    }
}
