package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthSummaryResponse {
    private Long checkId;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private Date checkedAt;
}
