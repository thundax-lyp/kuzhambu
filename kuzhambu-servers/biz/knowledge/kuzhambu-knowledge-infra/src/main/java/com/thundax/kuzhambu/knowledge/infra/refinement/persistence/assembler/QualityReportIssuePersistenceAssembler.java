package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportIssueDO;
import java.util.List;

public final class QualityReportIssuePersistenceAssembler {

    private QualityReportIssuePersistenceAssembler() {}

    public static QualityReportIssue toDomain(QualityReportIssueDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new QualityReportIssue(
                dataObject.getId(),
                dataObject.getIssueId(),
                dataObject.getReportId(),
                dataObject.getIssueType(),
                dataObject.getSeverity(),
                dataObject.getObjectType(),
                dataObject.getObjectKey(),
                dataObject.getTitle(),
                dataObject.getDescription(),
                dataObject.getSuggestion(),
                dataObject.getHref(),
                dataObject.getPriority(),
                dataObject.getCreatedAt());
    }

    public static QualityReportIssueDO toObject(QualityReportIssue entity) {
        if (entity == null) {
            return null;
        }
        return new QualityReportIssueDO(
                entity.getId(),
                entity.getIssueId(),
                entity.getReportId(),
                entity.getIssueType(),
                entity.getSeverity(),
                entity.getObjectType(),
                entity.getObjectKey(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSuggestion(),
                entity.getHref(),
                entity.getPriority(),
                entity.getCreatedAt());
    }

    public static List<QualityReportIssue> toDomainList(List<QualityReportIssueDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(QualityReportIssuePersistenceAssembler::toDomain)
                        .toList();
    }
}
