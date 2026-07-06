package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.QualityAnnotationPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityAnnotationDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityAnnotationMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QualityAnnotationRepositoryImpl implements QualityAnnotationRepository {

    private final QualityAnnotationMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QualityAnnotationRepositoryImpl(QualityAnnotationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<QualityAnnotation> listBySource(
            String objectType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
        QueryWrapper<QualityAnnotationDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(objectType), "object_type", objectType)
                .eq("source_content_type", sourceContentType)
                .eq("source_content_id", sourceContentId)
                .eq("graph_version_id", graphVersionId)
                .orderByDesc("updated_at")
                .orderByDesc("id");
        return QualityAnnotationPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public List<QualityAnnotation> listByGraphVersionId(Long graphVersionId) {
        QueryWrapper<QualityAnnotationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("graph_version_id", graphVersionId).orderByDesc("updated_at").orderByDesc("id");
        return QualityAnnotationPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdate(QualityAnnotation annotation) {
        QualityAnnotationDO dataObject = QualityAnnotationPersistenceAssembler.toObject(annotation);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getAnnotationId() == null) {
            dataObject.setAnnotationId(dataObject.getId());
        }
        int updated = mapper.update(
                null,
                new UpdateWrapper<QualityAnnotationDO>()
                        .eq("annotation_id", dataObject.getAnnotationId())
                        .set("object_type", dataObject.getObjectType())
                        .set("object_key", dataObject.getObjectKey())
                        .set("source_content_type", dataObject.getSourceContentType())
                        .set("source_content_id", dataObject.getSourceContentId())
                        .set("graph_version_id", dataObject.getGraphVersionId())
                        .set("annotation_status", dataObject.getAnnotationStatus())
                        .set("annotation_label", dataObject.getAnnotationLabel())
                        .set("comment", dataObject.getComment())
                        .set("updated_by", dataObject.getUpdatedBy())
                        .set("updated_at", dataObject.getUpdatedAt()));
        if (updated == 0) {
            mapper.insert(dataObject);
        }
    }

    @Override
    public int deleteByAnnotationId(Long annotationId) {
        QueryWrapper<QualityAnnotationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("annotation_id", annotationId);
        return mapper.delete(wrapper);
    }
}
