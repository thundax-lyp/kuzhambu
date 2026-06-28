package com.thundax.kuzhambu.classics.application.facade.assembler;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicsFacadeAssembler {

    public ClassicsSummaryFacadeResponse toFacadeResponse(ClassicsReportSummaryResult result) {
        if (result == null) {
            return null;
        }
        return ClassicsSummaryFacadeResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .contentCount(result.getContentCount())
                .translatedContentCount(result.getTranslatedContentCount())
                .imageReadyContentCount(result.getImageReadyContentCount())
                .visualAssetReadyContentCount(result.getVisualAssetReadyContentCount())
                .shareVisitCount(result.getShareVisitCount())
                .topContents(toTopContentFacadeDtos(result.getTopContents()))
                .contentGrowthSeries(toContentGrowthPointFacadeDtos(result.getContentGrowthSeries()))
                .build();
    }

    private List<ClassicsTopContentFacadeDto> toTopContentFacadeDtos(
            List<ClassicsReportSummaryResult.TopContentResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> ClassicsTopContentFacadeDto.builder()
                        .contentId(result.getContentId())
                        .contentType(result.getContentType())
                        .title(result.getTitle())
                        .visitCount(result.getVisitCount())
                        .build())
                .toList();
    }

    private List<ClassicsContentGrowthPointFacadeDto> toContentGrowthPointFacadeDtos(
            List<ClassicsReportSummaryResult.ContentGrowthPointResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> ClassicsContentGrowthPointFacadeDto.builder()
                        .bucket(result.getBucket())
                        .createdCount(result.getCreatedCount())
                        .build())
                .toList();
    }
}
