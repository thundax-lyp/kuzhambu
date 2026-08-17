package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;

public final class GraphExtractionTaskPersistenceAssembler {
    private static final String CURRENT_PIPELINE_VERSION = "graph-extraction-v1";

    private GraphExtractionTaskPersistenceAssembler() {}

    public static GraphExtractionTaskDO toObject(GraphExtractionTask entity) {
        if (entity == null) {
            return null;
        }
        return new GraphExtractionTaskDO(
                GraphExtractionTaskIdCodec.toValue(entity.getId()),
                entity.getMaterialId(),
                ContentRefCodec.toContentType(entity.getContentRef()),
                ContentRefCodec.toValue(entity.getContentRef()),
                entity.getContentSnapshotJson(),
                CURRENT_PIPELINE_VERSION,
                entity.getModelSnapshotJson(),
                entity.getPromptSnapshotJson(),
                entity.getOutputSchemaJson(),
                entity.getExecutionStatus() == null
                        ? null
                        : entity.getExecutionStatus().value(),
                entity.getDisposition() == null ? null : entity.getDisposition().value(),
                entity.getAttemptNo(),
                entity.getLockVersion(),
                entity.getBatchId(),
                entity.getAiBatchId(),
                entity.getCandidateId(),
                entity.getCurrentStage(),
                entity.getProgress(),
                entity.getIdempotencyKey(),
                GraphExtractionTaskIdCodec.toValue(entity.getRegeneratedFromTaskId()),
                GraphExtractionTaskIdCodec.toValue(entity.getSupersededByTaskId()),
                GraphExtractionTaskIdCodec.toValue(entity.getTriggeredByTaskId()),
                entity.getRequestedAt(),
                entity.getCompletedAt(),
                entity.getDisposedAt(),
                entity.getPurgeAfter());
    }

    public static GraphExtractionTask toDomain(GraphExtractionTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphExtractionTask(
                GraphExtractionTaskIdCodec.toDomain(dataObject.getId()),
                dataObject.getMaterialId(),
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                dataObject.getContentSnapshotJson(),
                dataObject.getModelSnapshotJson(),
                dataObject.getPromptSnapshotJson(),
                dataObject.getOutputSchemaJson(),
                GraphExtractionExecutionStatus.from(dataObject.getExecutionStatus()),
                GraphExtractionDisposition.from(dataObject.getDisposition()),
                dataObject.getAttemptNo(),
                dataObject.getLockVersion(),
                dataObject.getBatchId(),
                dataObject.getAiBatchId(),
                dataObject.getCandidateId(),
                dataObject.getCurrentStage(),
                dataObject.getProgress(),
                dataObject.getIdempotencyKey(),
                GraphExtractionTaskIdCodec.toDomain(dataObject.getRegeneratedFromTaskId()),
                GraphExtractionTaskIdCodec.toDomain(dataObject.getSupersededByTaskId()),
                GraphExtractionTaskIdCodec.toDomain(dataObject.getTriggeredByTaskId()),
                dataObject.getRequestedAt(),
                dataObject.getCompletedAt(),
                dataObject.getDisposedAt(),
                dataObject.getPurgeAfter());
    }
}
