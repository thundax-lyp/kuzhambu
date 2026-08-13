package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialEvent {
    private GraphMaterialEventId id;
    private ContentRef materialRef;
    private GraphMaterialEventType type;
    private GraphMaterialEventStatus status;
    private Instant changedAt;
    private long lockVersion;
}
