package com.thundax.kuzhambu.discovery.application.search.query;

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
public class SearchLogPageQuery {
    private String queryText;
    private List<String> intentTypes;
    private List<String> searchStatuses;
    private String operatorId;
    private Date dateFrom;
    private Date dateTo;
    private int pageNo;
    private int pageSize;
}
