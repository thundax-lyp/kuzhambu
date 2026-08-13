package com.thundax.kuzhambu.knowledge.domain.graph.material.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.material.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.material.model.valueobject.GraphMaterialId;
import com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.valueobject.GraphPipelineTaskId;
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
    private GraphMaterialId id;
    private ContentRef contentRef;
    private GraphMaterialStatus status;
    private Instant publishedAt;
    private GraphPipelineTaskId currentExtractionTaskId;
    private long version;

    public boolean editable() {
        return status == GraphMaterialStatus.DRAFT;
    }

    public void beginExtraction(GraphPipelineTaskId taskId) {
        if (!editable()) {
            throw new IllegalStateException("Only draft graph materials can start extraction");
        }
        status = GraphMaterialStatus.EXTRACTING;
        currentExtractionTaskId = taskId;
    }

    public void finishExtraction() {
        if (status != GraphMaterialStatus.EXTRACTING) {
            throw new IllegalStateException("Graph material is not extracting");
        }
        status = GraphMaterialStatus.DRAFT;
        currentExtractionTaskId = null;
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
        status = GraphMaterialStatus.DRAFT;
        publishedAt = null;
    }
}
