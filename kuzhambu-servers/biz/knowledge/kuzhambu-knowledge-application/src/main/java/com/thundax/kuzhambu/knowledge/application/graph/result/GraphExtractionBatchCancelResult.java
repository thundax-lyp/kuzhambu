package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionBatchCancelResult {
    private Long batchJobId;
    private String status;
    private Integer cancelledCount;
    private Integer completedCount;
    private Integer failedCount;
}
