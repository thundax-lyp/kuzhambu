package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagGovernanceMetrics {
    private List<TagUsageMetric> topTags;
    private List<CategoryDistributionMetric> categoryDistributions;
    private List<SourceRatioMetric> sourceRatios;
    private List<MonthlyNewTagMetric> monthlyNewTags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagUsageMetric {
        private String tagName;
        private Long contentRefCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDistributionMetric {
        private String categoryName;
        private Long tagCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceRatioMetric {
        private TagSource source;
        private Long tagCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyNewTagMetric {
        private String month;
        private Long tagCount;
    }
}
