package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeRelationPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeRelationMapper;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeRelationRepositoryImpl implements KnowledgeRelationRepository {

    private final KnowledgeRelationMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public KnowledgeRelationRepositoryImpl(KnowledgeRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys) {
        QueryWrapper<KnowledgeRelationDO> wrapper = new QueryWrapper<>();
        wrapper.in(relationKeys != null && !relationKeys.isEmpty(), "relation_key", relationKeys);
        return KnowledgeRelationPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeRelation> relations) {
        for (KnowledgeRelation relation : relations == null ? List.<KnowledgeRelation>of() : relations) {
            KnowledgeRelationDO dataObject = KnowledgeRelationPersistenceAssembler.toObject(relation);
            if (dataObject.getId() == null) {
                dataObject.setId(idGenerator.nextId().value());
            }
            if (dataObject.getRelationId() == null) {
                dataObject.setRelationId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<KnowledgeRelationDO>()
                            .eq("relation_key", dataObject.getRelationKey())
                            .set("source_entity_key", dataObject.getSourceEntityKey())
                            .set("target_entity_key", dataObject.getTargetEntityKey())
                            .set("source_name", dataObject.getSourceName())
                            .set("target_name", dataObject.getTargetName())
                            .set("relation_type", dataObject.getRelationType())
                            .set("evidence", dataObject.getEvidence())
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
