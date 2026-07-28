package com.thundax.kuzhambu.operations.application.report.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportArtifactResult {

    private String format;
    private String filename;
    private String contentType;
    private byte[] contentBytes;
    private Long sizeBytes;
    private String sha256;
    private Long storageObjectId;
}
