package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import java.util.List;

public interface QualityAnnotationRepository {

    List<QualityAnnotation> listBySource(
            String objectType, String sourceContentType, Long sourceContentId, Long graphVersionId);

    default List<QualityAnnotation> listByGraphVersionId(Long graphVersionId) {
        return List.of();
    }

    void saveOrUpdate(QualityAnnotation annotation);

    int deleteByAnnotationId(Long annotationId);
}
