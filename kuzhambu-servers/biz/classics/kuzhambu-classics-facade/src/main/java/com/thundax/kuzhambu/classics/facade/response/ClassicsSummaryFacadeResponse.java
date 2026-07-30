package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsSummaryFacadeResponse {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final Long contentCount;
    private final Long translatedContentCount;
    private final Long imageReadyContentCount;
    private final Long visualAssetReadyContentCount;
    private final Long shareVisitCount;
    private final List<ClassicsTopContentFacadeDto> topContents;
    private final List<ClassicsContentGrowthPointFacadeDto> contentGrowthSeries;
}
