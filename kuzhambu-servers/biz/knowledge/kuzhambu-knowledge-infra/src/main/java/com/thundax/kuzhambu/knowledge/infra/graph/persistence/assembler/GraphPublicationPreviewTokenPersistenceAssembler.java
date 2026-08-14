package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublicationPreviewTokenDO;

public final class GraphPublicationPreviewTokenPersistenceAssembler {

    private GraphPublicationPreviewTokenPersistenceAssembler() {}

    public static GraphPublicationPreviewTokenDO toObject(GraphPublicationPreviewToken entity, Long materialId) {
        if (entity == null) {
            return null;
        }
        return new GraphPublicationPreviewTokenDO(
                entity.getToken(),
                materialId,
                entity.getMaterialLockVersion(),
                entity.getSnapshotJson(),
                entity.getExpiresAt(),
                entity.getConsumedAt());
    }

    public static GraphPublicationPreviewToken toDomain(
            GraphPublicationPreviewTokenDO dataObject, ContentRef materialRef) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublicationPreviewToken(
                dataObject.getToken(),
                materialRef,
                dataObject.getMaterialLockVersion() == null ? 0L : dataObject.getMaterialLockVersion(),
                dataObject.getSnapshotJson(),
                dataObject.getExpiresAt(),
                dataObject.getConsumedAt());
    }
}
