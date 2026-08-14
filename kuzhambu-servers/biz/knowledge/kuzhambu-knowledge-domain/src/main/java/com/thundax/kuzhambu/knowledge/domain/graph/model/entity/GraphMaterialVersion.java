package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialVersionId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialVersion {
    private GraphMaterialVersionId id;
    private ContentRef materialRef;
    private long versionNo;
    private String snapshotJson;
    private Long publishedBy;
    private Instant publishedAt;
}
