package com.thundax.kuzhambu.classics.facade.request;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsSummaryFacadeRequest {

    private final Date periodStart;
    private final Date periodEnd;
    private final String bucketType;

    @Builder
    private ClassicsSummaryFacadeRequest(Date periodStart, Date periodEnd, String bucketType) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.bucketType = bucketType;
    }
}
