package com.thundax.kuzhambu.knowledge.application.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeReportApplicationServiceImplTest {

    @Test
    void summaryShouldMapCoverageTopTagsCategoryDistributionsAndMonthlyTags() {
        TagGovernanceMetricsRepository repository = mock(TagGovernanceMetricsRepository.class);
        KnowledgeReportApplicationServiceImpl service = new KnowledgeReportApplicationServiceImpl(repository);
        when(repository.getTagCoverageRate()).thenReturn(new BigDecimal("0.8750"));
        when(repository.getMetrics(10, 6))
                .thenReturn(new TagGovernanceMetrics(
                        List.of(new TagGovernanceMetrics.TagUsageMetric("礼制", 12L)),
                        List.of(new TagGovernanceMetrics.CategoryDistributionMetric("礼学", 8L)),
                        List.of(),
                        List.of(new TagGovernanceMetrics.MonthlyNewTagMetric("2025-01", 3L))));

        KnowledgeReportSummaryResult result =
                service.summary(new Date(1_718_000_000_000L), new Date(1_720_419_200_000L), "WEEK");

        assertEquals(new BigDecimal("0.8750"), result.getTagCoverageRate());
        assertEquals("礼制", result.getTopTags().get(0).getTagName());
        assertEquals(12L, result.getTopTags().get(0).getContentRefCount());
        assertEquals("礼学", result.getCategoryDistributions().get(0).getCategoryName());
        assertEquals(8L, result.getCategoryDistributions().get(0).getTagCount());
        assertEquals("2025-01", result.getMonthlyNewTags().get(0).getBucket());
        assertEquals(3L, result.getMonthlyNewTags().get(0).getTagCount());
    }
}
