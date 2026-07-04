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
public class QaKnowledgeSyncBatch {
    private Long id;
    private Long batchId;
    private String triggerType;
    private String provider;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Date startedAt;
    private Date finishedAt;
}
