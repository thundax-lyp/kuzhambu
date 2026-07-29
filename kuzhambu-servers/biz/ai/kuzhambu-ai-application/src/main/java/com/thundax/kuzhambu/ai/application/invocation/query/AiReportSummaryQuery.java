package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiReportSummaryQuery {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final AiReportBucketType bucketType;
}
