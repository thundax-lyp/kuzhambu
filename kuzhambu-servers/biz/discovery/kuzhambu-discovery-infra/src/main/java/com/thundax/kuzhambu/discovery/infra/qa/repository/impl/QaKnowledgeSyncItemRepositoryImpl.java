package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeSourceId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncItemRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaKnowledgeSyncItemDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaKnowledgeSyncItemMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class QaKnowledgeSyncItemRepositoryImpl implements QaKnowledgeSyncItemRepository {

    private final QaKnowledgeSyncItemMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QaKnowledgeSyncItemRepositoryImpl(QaKnowledgeSyncItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaKnowledgeSyncItem getBySourceId(KnowledgeSourceId sourceId) {
        if (sourceId == null) {
            return null;
        }
        return toDomain(mapper.selectOne(new QueryWrapper<QaKnowledgeSyncItemDO>()
                .eq("source_id", QaStringValueCodec.toValue(sourceId))
                .last("limit 1")));
    }

    @Override
    public List<QaKnowledgeSyncItem> listBySyncStatus(QaKnowledgeSyncStatus syncStatus, Integer limit) {
        if (syncStatus == null) {
            return List.of();
        }
        QueryWrapper<QaKnowledgeSyncItemDO> wrapper = new QueryWrapper<QaKnowledgeSyncItemDO>()
                .eq("sync_status", QaStringValueCodec.toValue(syncStatus))
                .orderByDesc("updated_at");
        if (limit != null && limit > 0) {
            wrapper.last("limit " + limit);
        }
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public KnowledgeSourceId save(QaKnowledgeSyncItem entity) {
        QaKnowledgeSyncItemDO dataObject = toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        mapper.insert(dataObject);
        return entity.getSourceId();
    }

    @Override
    public int update(QaKnowledgeSyncItem entity) {
        return mapper.updateById(toObject(entity));
    }

    private QaKnowledgeSyncItemDO toObject(QaKnowledgeSyncItem entity) {
        if (entity == null) {
            return null;
        }
        QaKnowledgeSyncItemDO dataObject = new QaKnowledgeSyncItemDO();
        dataObject.setId(entity.getId());
        dataObject.setSourceId(QaStringValueCodec.toValue(entity.getSourceId()));
        dataObject.setContentType(entity.getContentType());
        dataObject.setContentId(entity.getContentId());
        dataObject.setKnowledgeBaseName(entity.getKnowledgeBaseName());
        dataObject.setCurrentVersionNo(entity.getCurrentVersionNo());
        dataObject.setKnowledgeRevision(entity.getKnowledgeRevision());
        dataObject.setProvider(entity.getProvider());
        dataObject.setExternalKnowledgeBaseId(entity.getExternalKnowledgeBaseId());
        dataObject.setExternalKnowledgeItemId(entity.getExternalKnowledgeItemId());
        dataObject.setSyncStatus(QaStringValueCodec.toValue(entity.getSyncStatus()));
        dataObject.setFailureReason(entity.getFailureReason());
        dataObject.setSyncedAt(entity.getSyncedAt());
        dataObject.setCreatedAt(entity.getCreatedAt());
        dataObject.setUpdatedAt(entity.getUpdatedAt());
        return dataObject;
    }

    private QaKnowledgeSyncItem toDomain(QaKnowledgeSyncItemDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaKnowledgeSyncItem entity = new QaKnowledgeSyncItem();
        entity.setId(dataObject.getId());
        entity.setSourceId(dataObject.getSourceId());
        entity.setContentType(dataObject.getContentType());
        entity.setContentId(dataObject.getContentId());
        entity.setKnowledgeBaseName(dataObject.getKnowledgeBaseName());
        entity.setCurrentVersionNo(dataObject.getCurrentVersionNo());
        entity.setKnowledgeRevision(dataObject.getKnowledgeRevision());
        entity.setProvider(dataObject.getProvider());
        entity.setExternalKnowledgeBaseId(dataObject.getExternalKnowledgeBaseId());
        entity.setExternalKnowledgeItemId(dataObject.getExternalKnowledgeItemId());
        entity.setSyncStatus(dataObject.getSyncStatus());
        entity.setFailureReason(dataObject.getFailureReason());
        entity.setSyncedAt(dataObject.getSyncedAt());
        entity.setCreatedAt(dataObject.getCreatedAt());
        entity.setUpdatedAt(dataObject.getUpdatedAt());
        return entity;
    }
}
