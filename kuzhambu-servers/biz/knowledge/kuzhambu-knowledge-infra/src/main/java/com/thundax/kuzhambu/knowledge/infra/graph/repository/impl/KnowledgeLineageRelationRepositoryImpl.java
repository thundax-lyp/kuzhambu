package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeLineageRelationPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageRelationMapper;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeLineageRelationRepositoryImpl implements KnowledgeLineageRelationRepository {

    private final KnowledgeLineageRelationMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public KnowledgeLineageRelationRepositoryImpl(KnowledgeLineageRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeLineageRelation> listByRelationKeys(Collection<String> relationKeys) {
        QueryWrapper<KnowledgeLineageRelationDO> wrapper = new QueryWrapper<>();
        wrapper.in(relationKeys != null && !relationKeys.isEmpty(), "relation_key", relationKeys);
        return KnowledgeLineageRelationPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations) {
        for (KnowledgeLineageRelation relation : relations == null ? List.<KnowledgeLineageRelation>of() : relations) {
            KnowledgeLineageRelationDO dataObject = KnowledgeLineageRelationPersistenceAssembler.toObject(relation);
            if (dataObject.getId() == null) {
                dataObject.setId(idGenerator.nextId().value());
            }
            if (dataObject.getRelationId() == null) {
                dataObject.setRelationId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new LambdaUpdateWrapper<KnowledgeLineageRelationDO>()
                            .eq(KnowledgeLineageRelationDO::getRelationKey, dataObject.getRelationKey())
                            .set(KnowledgeLineageRelationDO::getSourceNodeKey, dataObject.getSourceNodeKey())
                            .set(KnowledgeLineageRelationDO::getTargetNodeKey, dataObject.getTargetNodeKey())
                            .set(KnowledgeLineageRelationDO::getSourceName, dataObject.getSourceName())
                            .set(KnowledgeLineageRelationDO::getTargetName, dataObject.getTargetName())
                            .set(KnowledgeLineageRelationDO::getRelationType, dataObject.getRelationType())
                            .set(KnowledgeLineageRelationDO::getEvidence, dataObject.getEvidence())
                            .set(KnowledgeLineageRelationDO::getConfirmationStatus, dataObject.getConfirmationStatus())
                            .set(KnowledgeLineageRelationDO::getLatestVersionId, dataObject.getLatestVersionId())
                            .set(KnowledgeLineageRelationDO::getSourceRefsJson, dataObject.getSourceRefsJson())
                            .set(KnowledgeLineageRelationDO::getFirstExtractedAt, dataObject.getFirstExtractedAt())
                            .set(KnowledgeLineageRelationDO::getLastExtractedAt, dataObject.getLastExtractedAt())
                            .set(KnowledgeLineageRelationDO::getConfirmedAt, dataObject.getConfirmedAt()));
            if (updated == 0) {
                mapper.insert(dataObject);
            }
        }
    }
}
