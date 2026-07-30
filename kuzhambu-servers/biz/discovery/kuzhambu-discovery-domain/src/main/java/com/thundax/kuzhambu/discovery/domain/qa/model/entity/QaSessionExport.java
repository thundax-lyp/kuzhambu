package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSessionExport {

    private Long id;
    private Long sessionId;
    private String format;
    private Long storageObjectId;
    private String exportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;

    public QaSessionExport(
            Long id,
            Long exportId,
            Long sessionId,
            String format,
            Long storageObjectId,
            String exportStatus,
            String failureReason,
            Long requesterUserId,
            Date requestedAt,
            Date completedAt) {
        this.id = id == null ? exportId : id;
        this.sessionId = sessionId;
        this.format = format;
        this.storageObjectId = storageObjectId;
        this.exportStatus = exportStatus;
        this.failureReason = failureReason;
        this.requesterUserId = requesterUserId;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public Long getExportId() {
        return id;
    }

    public void setExportId(Long exportId) {
        this.id = exportId;
    }
}
