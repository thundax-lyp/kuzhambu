package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportDO;
import java.util.List;

public final class QualityReportPersistenceAssembler {

    private QualityReportPersistenceAssembler() {}

    public static QualityReport toDomain(QualityReportDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new QualityReport(
                dataObject.getId(),
                dataObject.getReportId(),
                dataObject.getReportNo(),
                dataObject.getGraphVersionId(),
                dataObject.getSourceContentType(),
                dataObject.getSourceContentId(),
                dataObject.getSourceCategoryCode(),
                dataObject.getSourceCategoryName(),
                dataObject.getReportStatus(),
                dataObject.getEntityTotalCount(),
                dataObject.getEntityConfirmedCount(),
                dataObject.getRelationTotalCount(),
                dataObject.getRelationConfirmedCount(),
                dataObject.getLineageTotalCount(),
                dataObject.getLineageConfirmedCount(),
                dataObject.getEntityCoverageRate(),
                dataObject.getRelationAccuracyRate(),
                dataObject.getLineageCoverageRate(),
                dataObject.getCompletenessRate(),
                dataObject.getAnnotationCount(),
                dataObject.getIssueCount(),
                dataObject.getGeneratedBy(),
                dataObject.getGeneratedAt(),
                dataObject.getPublishedAt(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }

    public static QualityReportDO toObject(QualityReport entity) {
        if (entity == null) {
            return null;
        }
        return new QualityReportDO(
                entity.getId(),
                entity.getReportId(),
                entity.getReportNo(),
                entity.getGraphVersionId(),
                entity.getSourceContentType(),
                entity.getSourceContentId(),
                entity.getSourceCategoryCode(),
                entity.getSourceCategoryName(),
                entity.getReportStatus(),
                entity.getEntityTotalCount(),
                entity.getEntityConfirmedCount(),
                entity.getRelationTotalCount(),
                entity.getRelationConfirmedCount(),
                entity.getLineageTotalCount(),
                entity.getLineageConfirmedCount(),
                entity.getEntityCoverageRate(),
                entity.getRelationAccuracyRate(),
                entity.getLineageCoverageRate(),
                entity.getCompletenessRate(),
                entity.getAnnotationCount(),
                entity.getIssueCount(),
                entity.getGeneratedBy(),
                entity.getGeneratedAt(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static List<QualityReport> toDomainList(List<QualityReportDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(QualityReportPersistenceAssembler::toDomain)
                        .toList();
    }
}
