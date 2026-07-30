package com.thundax.kuzhambu.operations.application.dashboard.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsDashboardOverviewQuery {
    private String periodType;
    private Instant periodStart;
    private Instant periodEnd;
}
