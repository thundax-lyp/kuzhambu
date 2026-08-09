package com.thundax.kuzhambu.operations.application.health.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthQuery {
    private String component;
    private String healthStatus;
    private String probeSource;
    private String probeTarget;
    private Instant checkedAtStart;
    private Instant checkedAtEnd;
}
