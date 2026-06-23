package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeEntityPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeEntityMapper;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeEntityRepositoryImpl implements KnowledgeEntityRepository {

    private final KnowledgeEntityMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public KnowledgeEntityRepositoryImpl(KnowledgeEntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.in(entityKeys != null && !entityKeys.isEmpty(), "entity_key", entityKeys);
        return KnowledgeEntityPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeEntity> entities) {
        for (KnowledgeEntity entity : entities == null ? List.<KnowledgeEntity>of() : entities) {
            KnowledgeEntityDO dataObject = KnowledgeEntityPersistenceAssembler.toObject(entity);
            if (dataObject.getId() == null) {
                dataObject.setId(idGenerator.nextId().value());
            }
            if (dataObject.getEntityId() == null) {
                dataObject.setEntityId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new LambdaUpdateWrapper<KnowledgeEntityDO>()
                            .eq(KnowledgeEntityDO::getEntityKey, dataObject.getEntityKey())
                            .set(KnowledgeEntityDO::getName, dataObject.getName())
                            .set(KnowledgeEntityDO::getEntityType, dataObject.getEntityType())
                            .set(KnowledgeEntityDO::getDescription, dataObject.getDescription())
                            .set(KnowledgeEntityDO::getConfirmationStatus, dataObject.getConfirmationStatus())
                            .set(KnowledgeEntityDO::getLatestVersionId, dataObject.getLatestVersionId())
                            .set(KnowledgeEntityDO::getSourceRefsJson, dataObject.getSourceRefsJson())
                            .set(KnowledgeEntityDO::getFirstExtractedAt, dataObject.getFirstExtractedAt())
                            .set(KnowledgeEntityDO::getLastExtractedAt, dataObject.getLastExtractedAt())
                            .set(KnowledgeEntityDO::getConfirmedAt, dataObject.getConfirmedAt()));
            if (updated == 0) {
                mapper.insert(dataObject);
            }
        }
    }
}
