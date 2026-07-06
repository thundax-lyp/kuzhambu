package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportSourceDetailDO;
import java.util.List;

public final class QualityReportSourceDetailPersistenceAssembler {

    private QualityReportSourceDetailPersistenceAssembler() {}

    public static QualityReportSourceDetail toDomain(QualityReportSourceDetailDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new QualityReportSourceDetail(
                dataObject.getId(),
                dataObject.getDetailId(),
                dataObject.getReportId(),
                dataObject.getSourceContentType(),
                dataObject.getSourceContentId(),
                dataObject.getSourceCategoryCode(),
                dataObject.getSourceCategoryName(),
                dataObject.getGraphVersionId(),
                dataObject.getAppliedAt(),
                dataObject.getAnnotationCount(),
                dataObject.getIssueCount(),
                dataObject.getStatus(),
                dataObject.getHref(),
                dataObject.getCreatedAt());
    }

    public static QualityReportSourceDetailDO toObject(QualityReportSourceDetail entity) {
        if (entity == null) {
            return null;
        }
        return new QualityReportSourceDetailDO(
                entity.getId(),
                entity.getDetailId(),
                entity.getReportId(),
                entity.getSourceContentType(),
                entity.getSourceContentId(),
                entity.getSourceCategoryCode(),
                entity.getSourceCategoryName(),
                entity.getGraphVersionId(),
                entity.getAppliedAt(),
                entity.getAnnotationCount(),
                entity.getIssueCount(),
                entity.getStatus(),
                entity.getHref(),
                entity.getCreatedAt());
    }

    public static List<QualityReportSourceDetail> toDomainList(List<QualityReportSourceDetailDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(QualityReportSourceDetailPersistenceAssembler::toDomain)
                        .toList();
    }
}
