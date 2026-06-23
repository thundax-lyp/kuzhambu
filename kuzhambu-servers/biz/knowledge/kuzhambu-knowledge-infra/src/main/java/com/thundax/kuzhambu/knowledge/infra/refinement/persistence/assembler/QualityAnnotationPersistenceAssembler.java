package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityAnnotationDO;
import java.util.List;

public final class QualityAnnotationPersistenceAssembler {

    private QualityAnnotationPersistenceAssembler() {}

    public static QualityAnnotation toDomain(QualityAnnotationDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new QualityAnnotation(
                dataObject.getId(),
                dataObject.getAnnotationId(),
                dataObject.getObjectType(),
                dataObject.getObjectKey(),
                dataObject.getSourceContentType(),
                dataObject.getSourceContentId(),
                dataObject.getGraphVersionId(),
                dataObject.getAnnotationStatus(),
                dataObject.getAnnotationLabel(),
                dataObject.getComment(),
                dataObject.getCreatedBy(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }

    public static QualityAnnotationDO toObject(QualityAnnotation entity) {
        if (entity == null) {
            return null;
        }
        return new QualityAnnotationDO(
                entity.getId(),
                entity.getAnnotationId(),
                entity.getObjectType(),
                entity.getObjectKey(),
                entity.getSourceContentType(),
                entity.getSourceContentId(),
                entity.getGraphVersionId(),
                entity.getAnnotationStatus(),
                entity.getAnnotationLabel(),
                entity.getComment(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt());
    }

    public static List<QualityAnnotation> toDomainList(List<QualityAnnotationDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(QualityAnnotationPersistenceAssembler::toDomain)
                        .toList();
    }
}
