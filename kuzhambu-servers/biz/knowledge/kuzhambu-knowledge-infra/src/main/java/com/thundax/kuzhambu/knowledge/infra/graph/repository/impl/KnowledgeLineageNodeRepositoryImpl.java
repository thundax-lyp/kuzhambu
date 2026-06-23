package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeLineageNodePersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageNodeMapper;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeLineageNodeRepositoryImpl implements KnowledgeLineageNodeRepository {

    private final KnowledgeLineageNodeMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public KnowledgeLineageNodeRepositoryImpl(KnowledgeLineageNodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeLineageNode> listByNodeKeys(Collection<String> nodeKeys) {
        QueryWrapper<KnowledgeLineageNodeDO> wrapper = new QueryWrapper<>();
        wrapper.in(nodeKeys != null && !nodeKeys.isEmpty(), "node_key", nodeKeys);
        return KnowledgeLineageNodePersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeLineageNode> nodes) {
        for (KnowledgeLineageNode node : nodes == null ? List.<KnowledgeLineageNode>of() : nodes) {
            KnowledgeLineageNodeDO dataObject = KnowledgeLineageNodePersistenceAssembler.toObject(node);
            if (dataObject.getId() == null) {
                dataObject.setId(idGenerator.nextId().value());
            }
            if (dataObject.getNodeId() == null) {
                dataObject.setNodeId(dataObject.getId());
            }
            int updated = mapper.update(
                    null,
                    new UpdateWrapper<KnowledgeLineageNodeDO>()
                            .eq("node_key", dataObject.getNodeKey())
                            .set("name", dataObject.getName())
                            .set("node_type", dataObject.getNodeType())
                            .set("generation", dataObject.getGeneration())
                            .set("gender", dataObject.getGender())
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
