package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionTaskDeleteReceipt {
    private String idempotencyKey;
    private GraphExtractionTaskId deletedTaskId;
    private Instant completedAt;
}
