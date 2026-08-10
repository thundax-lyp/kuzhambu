package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import java.math.BigDecimal;

public interface TagGovernanceMetricsRepository {

    TagGovernanceMetrics getByTopLimitAndRecentMonths(Integer topLimit, Integer recentMonths);

    BigDecimal getByTagCoverageRate();
}
