package com.thundax.kuzhambu.classics.facade.request;

import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsSummaryFacadeRequest {

    private final Date periodStart;
    private final Date periodEnd;
    private final String bucketType;
}
