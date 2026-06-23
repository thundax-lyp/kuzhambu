package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
                    new UpdateWrapper<KnowledgeLineageRelationDO>()
                            .eq("relation_key", dataObject.getRelationKey())
                            .set("source_node_key", dataObject.getSourceNodeKey())
                            .set("target_node_key", dataObject.getTargetNodeKey())
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
