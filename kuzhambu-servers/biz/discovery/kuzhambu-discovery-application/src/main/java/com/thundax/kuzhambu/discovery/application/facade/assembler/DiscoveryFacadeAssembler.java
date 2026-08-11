package com.thundax.kuzhambu.discovery.application.facade.assembler;

import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryQaTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoverySearchTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryTopQueryFacadeDto;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryFacadeAssembler {

    public DiscoverySummaryFacadeResponse toSummaryFacadeResponse(DiscoveryReportSummaryResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoverySummaryFacadeResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .searchCount(result.getSearchCount())
                .qaCount(result.getQaCount())
                .avgSearchLatencyMs(result.getAvgSearchLatencyMs())
                .topQueries(toTopQueryFacadeDtos(result.getTopQueries()))
                .searchTrendSeries(toSearchTrendPointFacadeDtos(result.getSearchTrendSeries()))
                .qaTrendSeries(toQaTrendPointFacadeDtos(result.getQaTrendSeries()))
                .build();
    }

    private List<DiscoveryTopQueryFacadeDto> toTopQueryFacadeDtos(
            List<DiscoveryReportSummaryResult.TopQueryResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> DiscoveryTopQueryFacadeDto.builder()
                        .queryText(result.getQueryText())
                        .count(result.getCount())
                        .build())
                .toList();
    }

    private List<DiscoverySearchTrendPointFacadeDto> toSearchTrendPointFacadeDtos(
            List<DiscoveryReportSummaryResult.SearchTrendPointResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> DiscoverySearchTrendPointFacadeDto.builder()
                        .bucket(result.getBucket())
                        .searchCount(result.getSearchCount())
                        .build())
                .toList();
    }

    private List<DiscoveryQaTrendPointFacadeDto> toQaTrendPointFacadeDtos(
            List<DiscoveryReportSummaryResult.QaTrendPointResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> DiscoveryQaTrendPointFacadeDto.builder()
                        .bucket(result.getBucket())
                        .qaCount(result.getQaCount())
                        .build())
                .toList();
    }
}
