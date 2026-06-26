package com.thundax.kuzhambu.knowledge.application.report.result;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeReportSummaryResult {

    private Date periodStart;
    private Date periodEnd;
    private BigDecimal tagCoverageRate;
    private List<TopTagResult> topTags;
    private List<CategoryDistributionResult> categoryDistributions;
    private List<MonthlyNewTagResult> monthlyNewTags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTagResult {

        private String tagName;
        private Long contentRefCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDistributionResult {

        private String categoryName;
        private Long tagCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyNewTagResult {

        private String bucket;
        private Long tagCount;
    }
}
