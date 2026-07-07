package com.thundax.kuzhambu.operations.application.health.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthAlertPageQuery {
    private String component;
    private String alertLevel;
    private String alertStatus;
    private String sourceRefType;
    private Long sourceRefId;
}
