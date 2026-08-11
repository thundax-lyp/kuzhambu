package com.thundax.kuzhambu.discovery.application.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.report.query.DiscoveryReportSummaryQuery;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchEventRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryReportApplicationServiceImplTest {

    @Test
    void summaryShouldAggregateSearchQaTopQueryAndWeeklyBuckets() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        QaSessionRepository qaSessionRepository = mock(QaSessionRepository.class);
        DiscoveryReportApplicationServiceImpl service =
                new DiscoveryReportApplicationServiceImpl(searchEventRepository, qaSessionRepository);
        when(searchEventRepository.listByCreatedAtRange(date(1_718_000_000_000L), date(1_720_419_200_000L)))
                .thenReturn(List.of(
                        searchEvent("礼制", null, null, 100L, date(1_718_000_000_000L)),
                        searchEvent("原始礼制", "礼制", null, 200L, date(1_718_172_800_000L)),
                        searchEvent("原始礼制检索", "礼制归一", "礼制", null, date(1_718_259_200_000L)),
                        searchEvent("祭祀", null, null, null, date(1_718_777_600_000L))));
        when(qaSessionRepository.listByOpenedAtRange(date(1_718_000_000_000L), date(1_720_419_200_000L)))
                .thenReturn(List.of(qaSession(date(1_718_086_400_000L)), qaSession(date(1_718_864_000_000L))));

        DiscoveryReportSummaryResult result = service.summary(
                new DiscoveryReportSummaryQuery(date(1_718_000_000_000L), date(1_720_419_200_000L), "WEEK"));

        assertEquals(4L, result.getSearchCount());
        assertEquals(2L, result.getQaCount());
        assertEquals(150L, result.getAvgSearchLatencyMs());
        assertEquals("礼制", result.getTopQueries().get(0).getQueryText());
        assertEquals(3L, result.getTopQueries().get(0).getCount());
        assertEquals("2024-W24", result.getSearchTrendSeries().get(0).getBucket());
        assertEquals(3L, result.getSearchTrendSeries().get(0).getSearchCount());
        assertEquals("2024-W24", result.getQaTrendSeries().get(0).getBucket());
        assertEquals(1L, result.getQaTrendSeries().get(0).getQaCount());
    }

    @Test
    void summaryShouldReturnZeroAverageSearchLatencyWhenNoLatencySamples() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        QaSessionRepository qaSessionRepository = mock(QaSessionRepository.class);
        DiscoveryReportApplicationServiceImpl service =
                new DiscoveryReportApplicationServiceImpl(searchEventRepository, qaSessionRepository);
        when(searchEventRepository.listByCreatedAtRange(date(1_718_000_000_000L), date(1_720_419_200_000L)))
                .thenReturn(List.of(
                        searchEvent("礼制", null, null, null, date(1_718_000_000_000L)),
                        searchEvent("祭祀", null, null, null, date(1_718_777_600_000L))));
        when(qaSessionRepository.listByOpenedAtRange(date(1_718_000_000_000L), date(1_720_419_200_000L)))
                .thenReturn(List.of());

        DiscoveryReportSummaryResult result = service.summary(
                new DiscoveryReportSummaryQuery(date(1_718_000_000_000L), date(1_720_419_200_000L), "DAY"));

        assertEquals(2L, result.getSearchCount());
        assertEquals(0L, result.getAvgSearchLatencyMs());
    }

    private static SearchEvent searchEvent(
            String queryText,
            String normalizedQueryText,
            String displayQueryText,
            Long searchLatencyMs,
            Instant createdAt) {
        SearchEvent searchEvent = new SearchEvent();
        searchEvent.setQueryText(queryText);
        searchEvent.setNormalizedQueryText(normalizedQueryText);
        searchEvent.setDisplayQueryText(displayQueryText);
        searchEvent.setSearchLatencyMs(searchLatencyMs);
        searchEvent.setCreatedAt(createdAt);
        return searchEvent;
    }

    private static QaSession qaSession(Instant openedAt) {
        QaSession qaSession = new QaSession();
        qaSession.setOpenedAt(openedAt);
        return qaSession;
    }

    private static Instant date(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }
}
