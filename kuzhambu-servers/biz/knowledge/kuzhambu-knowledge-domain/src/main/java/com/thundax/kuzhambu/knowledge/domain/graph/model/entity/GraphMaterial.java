package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterial {
    private ContentRef contentRef;
    private String contentTitleSnapshot;
    private GraphMaterialStatus status;
    private Instant publishedAt;
    private long lockVersion;

    public boolean editable() {
        return status == GraphMaterialStatus.DRAFT || status == GraphMaterialStatus.READY;
    }

    public void markReady() {
        if (editable()) {
            status = GraphMaterialStatus.READY;
        }
    }

    public void markDraft() {
        if (editable()) {
            status = GraphMaterialStatus.DRAFT;
        }
    }

    public void beginPublication() {
        if (!editable()) {
            throw new IllegalStateException("Only draft graph materials can publish");
        }
        status = GraphMaterialStatus.PUBLISHING;
    }

    public void finishPublication(Instant completedAt) {
        if (status != GraphMaterialStatus.PUBLISHING) {
            throw new IllegalStateException("Graph material is not publishing");
        }
        status = GraphMaterialStatus.PUBLISHED;
        publishedAt = completedAt;
    }

    public void withdraw() {
        if (status != GraphMaterialStatus.PUBLISHED) {
            throw new IllegalStateException("Only published graph materials can withdraw");
        }
        status = GraphMaterialStatus.READY;
    }
}
