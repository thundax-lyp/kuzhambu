package com.thundax.kuzhambu.discovery.facade.response;

import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryQaTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoverySearchTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryTopQueryFacadeDto;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySummaryFacadeResponse {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final Long searchCount;
    private final Long qaCount;
    private final Long avgSearchLatencyMs;
    private final List<DiscoveryTopQueryFacadeDto> topQueries;
    private final List<DiscoverySearchTrendPointFacadeDto> searchTrendSeries;
    private final List<DiscoveryQaTrendPointFacadeDto> qaTrendSeries;
}
