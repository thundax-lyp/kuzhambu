package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagGovernanceMetricsQuery {
    private Integer topLimit;
    private Integer recentMonths;
}
