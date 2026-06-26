package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response;

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
public class OperationsReportGenerateResponse {
    private Long reportId;
    private String reportStatus;
}
