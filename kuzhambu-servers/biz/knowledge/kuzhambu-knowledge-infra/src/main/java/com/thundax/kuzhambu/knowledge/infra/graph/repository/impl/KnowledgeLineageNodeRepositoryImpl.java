package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
                    new LambdaUpdateWrapper<KnowledgeLineageNodeDO>()
                            .eq(KnowledgeLineageNodeDO::getNodeKey, dataObject.getNodeKey())
                            .set(KnowledgeLineageNodeDO::getName, dataObject.getName())
                            .set(KnowledgeLineageNodeDO::getNodeType, dataObject.getNodeType())
                            .set(KnowledgeLineageNodeDO::getGeneration, dataObject.getGeneration())
                            .set(KnowledgeLineageNodeDO::getGender, dataObject.getGender())
                            .set(KnowledgeLineageNodeDO::getConfirmationStatus, dataObject.getConfirmationStatus())
                            .set(KnowledgeLineageNodeDO::getLatestVersionId, dataObject.getLatestVersionId())
                            .set(KnowledgeLineageNodeDO::getSourceRefsJson, dataObject.getSourceRefsJson())
                            .set(KnowledgeLineageNodeDO::getFirstExtractedAt, dataObject.getFirstExtractedAt())
                            .set(KnowledgeLineageNodeDO::getLastExtractedAt, dataObject.getLastExtractedAt())
                            .set(KnowledgeLineageNodeDO::getConfirmedAt, dataObject.getConfirmedAt()));
            if (updated == 0) {
                mapper.insert(dataObject);
            }
        }
    }
}
