package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
                    new UpdateWrapper<KnowledgeEntityDO>()
                            .eq("entity_key", dataObject.getEntityKey())
                            .set("name", dataObject.getName())
                            .set("entity_type", dataObject.getEntityType())
                            .set("description", dataObject.getDescription())
                            .set("confirmation_status", dataObject.getConfirmationStatus())
                            .set("latest_version_id", dataObject.getLatestVersionId())
                            .set("source_refs_json", dataObject.getSourceRefsJson())
                            .set("first_extracted_at", dataObject.getFirstExtractedAt())
                            .set("last_extracted_at", dataObject.getLastExtractedAt())
                            .set("confirmed_at", dataObject.getConfirmedAt()));
            if (updated == 0) {
                mapper.insert(dataObject);
            }
        }
    }
}
