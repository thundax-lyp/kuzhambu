package com.thundax.kuzhambu.discovery.application.qa.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSessionPageQuery {
    private Long ownerUserId;
    private String status;
    private String scope;
    private int pageNo;
    private int pageSize;
}
