package com.thundax.kuzhambu.discovery.application.search.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchStatisticsSummaryQuery {
    private Instant dateFrom;
    private Instant dateTo;
}
