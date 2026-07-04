package com.thundax.kuzhambu.discovery.application.qa.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSessionExportResult {
    private Long exportId;
    private Long sessionId;
    private String format;
    private Long storageObjectId;
    private String exportStatus;
    private String failureReason;
    private Long requestedAt;
    private Long completedAt;
    private String filename;
    private String contentType;
}
