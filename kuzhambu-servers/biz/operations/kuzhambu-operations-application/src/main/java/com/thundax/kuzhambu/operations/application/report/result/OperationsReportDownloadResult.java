package com.thundax.kuzhambu.operations.application.report.result;

import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportDownloadResult {

    private ReportId reportId;
    private String format;
    private String artifactFilename;
    private String contentType;
    private Long contentLength;
    private String storageOriginalFilename;
    private InputStream inputStream;
}
