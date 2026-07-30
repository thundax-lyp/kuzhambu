package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEntity {
    private KnowledgeEntityId id;
    private String entityKey;
    private String name;
    private String entityType;
    private String description;
    private KnowledgeConfirmationStatus confirmationStatus;
    private GraphVersionId latestVersionId;
    private String sourceRefsJson;
    private Instant firstExtractedAt;
    private Instant lastExtractedAt;
    private Instant confirmedAt;
}
