package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;

public interface TagGovernanceMetricsRepository {

    TagGovernanceMetrics getMetrics(Integer topLimit, Integer recentMonths);
}
