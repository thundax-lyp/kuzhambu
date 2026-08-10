package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.KnowledgeEntityPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeEntityMapper;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeEntityRepositoryImpl implements KnowledgeEntityRepository {

    private final KnowledgeEntityMapper mapper;

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
    public List<KnowledgeEntity> listByVersionId(GraphVersionId versionId) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.eq("latest_version_id", GraphVersionIdCodec.toValue(versionId)).orderByAsc("id");
        return KnowledgeEntityPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public KnowledgeEntity getByEntityId(KnowledgeEntityId entityId) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", KnowledgeEntityIdCodec.toValue(entityId));
        return KnowledgeEntityPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public KnowledgeEntity getByEntityKey(String entityKey) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_key", entityKey);
        return KnowledgeEntityPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<KnowledgeEntity> page(
            GraphVersionId versionId,
            String keyword,
            String entityType,
            KnowledgeConfirmationStatus confirmationStatus,
            int pageNo,
            int pageSize) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.eq(versionId != null, "latest_version_id", GraphVersionIdCodec.toValue(versionId))
                .like(StringUtils.isNotBlank(keyword), "name", keyword)
                .eq(StringUtils.isNotBlank(entityType), "entity_type", entityType)
                .eq(
                        confirmationStatus != null,
                        "confirmation_status",
                        confirmationStatus == null ? null : confirmationStatus.value())
                .orderByDesc("last_extracted_at")
                .orderByDesc("id");
        IPage<KnowledgeEntityDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                KnowledgeEntityPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public void batchSaveOrUpdate(List<KnowledgeEntity> entities) {
        for (KnowledgeEntity entity : entities == null ? List.<KnowledgeEntity>of() : entities) {
            KnowledgeEntityDO dataObject = KnowledgeEntityPersistenceAssembler.toObject(entity);
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

    @Override
    public int deleteByEntityKeys(Collection<String> entityKeys) {
        QueryWrapper<KnowledgeEntityDO> wrapper = new QueryWrapper<>();
        wrapper.in(entityKeys != null && !entityKeys.isEmpty(), "entity_key", entityKeys);
        return mapper.delete(wrapper);
    }
}
