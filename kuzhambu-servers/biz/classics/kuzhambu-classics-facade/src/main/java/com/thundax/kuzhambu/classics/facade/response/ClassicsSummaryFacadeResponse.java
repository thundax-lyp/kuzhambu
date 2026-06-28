package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsSummaryFacadeResponse {

    private final Date periodStart;
    private final Date periodEnd;
    private final Long contentCount;
    private final Long translatedContentCount;
    private final Long imageReadyContentCount;
    private final Long visualAssetReadyContentCount;
    private final Long shareVisitCount;
    private final List<ClassicsTopContentFacadeDto> topContents;
    private final List<ClassicsContentGrowthPointFacadeDto> contentGrowthSeries;

    @Builder
    private ClassicsSummaryFacadeResponse(
            Date periodStart,
            Date periodEnd,
            Long contentCount,
            Long translatedContentCount,
            Long imageReadyContentCount,
            Long visualAssetReadyContentCount,
            Long shareVisitCount,
            List<ClassicsTopContentFacadeDto> topContents,
            List<ClassicsContentGrowthPointFacadeDto> contentGrowthSeries) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.contentCount = contentCount;
        this.translatedContentCount = translatedContentCount;
        this.imageReadyContentCount = imageReadyContentCount;
        this.visualAssetReadyContentCount = visualAssetReadyContentCount;
        this.shareVisitCount = shareVisitCount;
        this.topContents = topContents;
        this.contentGrowthSeries = contentGrowthSeries;
    }
}
