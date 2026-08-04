package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeLineageRelationPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageRelationMapper;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeLineageRelationRepositoryImpl implements KnowledgeLineageRelationRepository {

    private final KnowledgeLineageRelationMapper mapper;

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
    public List<KnowledgeLineageRelation> listByVersionId(Long versionId) {
        QueryWrapper<KnowledgeLineageRelationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("latest_version_id", versionId).orderByAsc("id");
        return KnowledgeLineageRelationPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public KnowledgeLineageRelation getByRelationId(Long relationId) {
        QueryWrapper<KnowledgeLineageRelationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", relationId);
        return KnowledgeLineageRelationPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<KnowledgeLineageRelation> page(
            Long versionId, String keyword, String relationType, String confirmationStatus, int pageNo, int pageSize) {
        QueryWrapper<KnowledgeLineageRelationDO> wrapper = new QueryWrapper<>();
        wrapper.eq(versionId != null, "latest_version_id", versionId)
                .and(
                        StringUtils.isNotBlank(keyword),
                        query -> query.like("source_name", keyword).or().like("target_name", keyword))
                .eq(StringUtils.isNotBlank(relationType), "relation_type", relationType)
                .eq(StringUtils.isNotBlank(confirmationStatus), "confirmation_status", confirmationStatus)
                .orderByDesc("last_extracted_at")
                .orderByDesc("id");
        IPage<KnowledgeLineageRelationDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                KnowledgeLineageRelationPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations) {
        for (KnowledgeLineageRelation relation : relations == null ? List.<KnowledgeLineageRelation>of() : relations) {
            KnowledgeLineageRelationDO dataObject = KnowledgeLineageRelationPersistenceAssembler.toObject(relation);
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

    @Override
    public int deleteByRelationKeys(Collection<String> relationKeys) {
        QueryWrapper<KnowledgeLineageRelationDO> wrapper = new QueryWrapper<>();
        wrapper.in(relationKeys != null && !relationKeys.isEmpty(), "relation_key", relationKeys);
        return mapper.delete(wrapper);
    }
}
