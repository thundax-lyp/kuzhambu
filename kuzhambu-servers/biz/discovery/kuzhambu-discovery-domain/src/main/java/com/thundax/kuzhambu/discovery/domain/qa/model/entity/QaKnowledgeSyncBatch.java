package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import java.time.Instant;
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
    private String triggerType;
    private String provider;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Instant startedAt;
    private Instant finishedAt;

    public QaKnowledgeSyncBatch(
            Long id,
            Long batchId,
            String triggerType,
            String provider,
            Integer totalCount,
            Integer successCount,
            Integer failureCount,
            Instant startedAt,
            Instant finishedAt) {
        this.id = id == null ? batchId : id;
        this.triggerType = triggerType;
        this.provider = provider;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long getBatchId() {
        return id;
    }

    public void setBatchId(Long batchId) {
        this.id = batchId;
    }
}
