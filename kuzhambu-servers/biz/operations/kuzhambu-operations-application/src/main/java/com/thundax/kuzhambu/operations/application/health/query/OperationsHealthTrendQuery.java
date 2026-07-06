package com.thundax.kuzhambu.operations.application.health.query;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthTrendQuery {
    private String component;
    private String probeSource;
    private Date periodStart;
    private Date periodEnd;
    private String bucketType;
}
