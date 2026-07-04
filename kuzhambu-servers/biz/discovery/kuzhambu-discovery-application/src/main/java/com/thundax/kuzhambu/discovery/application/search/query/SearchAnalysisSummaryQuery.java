package com.thundax.kuzhambu.discovery.application.search.query;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchAnalysisSummaryQuery {
    private Date dateFrom;
    private Date dateTo;
}
