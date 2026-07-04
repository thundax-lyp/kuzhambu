package com.thundax.kuzhambu.discovery.application.qa.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSyncBatchResult {
    private Long batchId;
    private String triggerType;
    private String provider;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Long startedAt;
    private Long finishedAt;
}
