package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeLineageNodePersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageNodeMapper;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeLineageNodeRepositoryImpl implements KnowledgeLineageNodeRepository {

    private final KnowledgeLineageNodeMapper mapper;

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
    public List<KnowledgeLineageNode> listByVersionId(Long versionId) {
        QueryWrapper<KnowledgeLineageNodeDO> wrapper = new QueryWrapper<>();
        wrapper.eq("latest_version_id", versionId).orderByAsc("id");
        return KnowledgeLineageNodePersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public KnowledgeLineageNode getByNodeId(Long nodeId) {
        QueryWrapper<KnowledgeLineageNodeDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", nodeId);
        return KnowledgeLineageNodePersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<KnowledgeLineageNode> page(
            Long versionId, String keyword, String nodeType, String confirmationStatus, int pageNo, int pageSize) {
        QueryWrapper<KnowledgeLineageNodeDO> wrapper = new QueryWrapper<>();
        wrapper.eq(versionId != null, "latest_version_id", versionId)
                .like(StringUtils.isNotBlank(keyword), "name", keyword)
                .eq(StringUtils.isNotBlank(nodeType), "node_type", nodeType)
                .eq(StringUtils.isNotBlank(confirmationStatus), "confirmation_status", confirmationStatus)
                .orderByDesc("last_extracted_at")
                .orderByDesc("id");
        IPage<KnowledgeLineageNodeDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                KnowledgeLineageNodePersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeLineageNode> nodes) {
        for (KnowledgeLineageNode node : nodes == null ? List.<KnowledgeLineageNode>of() : nodes) {
            KnowledgeLineageNodeDO dataObject = KnowledgeLineageNodePersistenceAssembler.toObject(node);
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

    @Override
    public int deleteByNodeKeys(Collection<String> nodeKeys) {
        QueryWrapper<KnowledgeLineageNodeDO> wrapper = new QueryWrapper<>();
        wrapper.in(nodeKeys != null && !nodeKeys.isEmpty(), "node_key", nodeKeys);
        return mapper.delete(wrapper);
    }
}
