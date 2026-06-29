package com.thundax.kuzhambu.operations.application.health.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthPageQuery {
    private String component;
    private String healthStatus;
}
