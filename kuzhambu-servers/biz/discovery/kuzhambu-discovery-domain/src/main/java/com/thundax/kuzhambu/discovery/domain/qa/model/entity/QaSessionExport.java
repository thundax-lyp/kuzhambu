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
    private Long exportId;
    private Long sessionId;
    private String format;
    private Long storageObjectId;
    private String exportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;
}
