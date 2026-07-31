package com.thundax.kuzhambu.classics.infra.wangqi.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocumentEvent;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler.WangqiDocumentEventPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler.WangqiDocumentPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentEventDO;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper.WangqiDocumentEventMapper;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper.WangqiDocumentMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class WangqiDocumentRepositoryImpl implements WangqiDocumentRepository {

    private final WangqiDocumentMapper mapper;
    private final WangqiDocumentEventMapper eventMapper;

    public WangqiDocumentRepositoryImpl(WangqiDocumentMapper mapper, WangqiDocumentEventMapper eventMapper) {
        this.mapper = mapper;
        this.eventMapper = eventMapper;
    }

    @Override
    public WangqiDocument getById(WangqiDocumentId id) {
        WangqiDocument document =
                WangqiDocumentPersistenceAssembler.toDomain(mapper.selectById(WangqiDocumentIdCodec.toValue(id)));
        if (document != null) {
            attachEvents(List.of(document));
        }
        return document;
    }

    @Override
    public PageResult<WangqiDocument> page(
            String keyword, String visibility, SortDirection sortDirection, int pageNo, int pageSize) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = buildWrapper(keyword, visibility, sortDirection);
        Page<WangqiDocumentDO> dataPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<WangqiDocument> documents = WangqiDocumentPersistenceAssembler.toDomainList(dataPage.getRecords());
        attachEvents(documents);
        return PageResult.of((int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), documents);
    }

    @Override
    public List<WangqiDocument> listTimeline(String keyword, String visibility, SortDirection sortDirection) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = buildWrapper(keyword, visibility, sortDirection);
        List<WangqiDocument> documents = WangqiDocumentPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
        attachEvents(documents);
        return documents;
    }

    @Override
    public List<WangqiDocumentEvent> listEvents(List<WangqiDocumentId> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }
        List<Long> ids = documentIds.stream()
                .map(WangqiDocumentIdCodec::toValue)
                .filter(id -> id != null)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return WangqiDocumentEventPersistenceAssembler.toDomainList(
                eventMapper.selectList(new LambdaQueryWrapper<WangqiDocumentEventDO>()
                        .in(WangqiDocumentEventDO::getDocumentId, ids)
                        .orderByAsc(WangqiDocumentEventDO::getDocumentId)
                        .orderByAsc(WangqiDocumentEventDO::getPriority)
                        .orderByAsc(WangqiDocumentEventDO::getOccurredAt)
                        .orderByAsc(WangqiDocumentEventDO::getId)));
    }

    @Override
    public WangqiDocumentId insert(WangqiDocument document) {
        WangqiDocumentDO dataObject = WangqiDocumentPersistenceAssembler.toObject(document);
        mapper.insert(dataObject);
        return WangqiDocumentIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(WangqiDocument document) {
        return mapper.updateById(WangqiDocumentPersistenceAssembler.toObject(document));
    }

    @Override
    public int updateRestoredVersion(WangqiDocument document) {
        WangqiDocumentDO dataObject = WangqiDocumentPersistenceAssembler.toObject(document);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<WangqiDocumentDO>()
                        .eq(WangqiDocumentDO::getId, dataObject.getId())
                        .set(WangqiDocumentDO::getTitle, dataObject.getTitle())
                        .set(WangqiDocumentDO::getSummary, dataObject.getSummary())
                        .set(WangqiDocumentDO::getContentFormat, dataObject.getContentFormat())
                        .set(WangqiDocumentDO::getContent, dataObject.getContent())
                        .set(WangqiDocumentDO::getDocumentTime, dataObject.getDocumentTime())
                        .set(WangqiDocumentDO::getStorageObjectId, dataObject.getStorageObjectId())
                        .set(WangqiDocumentDO::getCurrentVersionId, dataObject.getCurrentVersionId())
                        .set(WangqiDocumentDO::getCurrentVersionNo, dataObject.getCurrentVersionNo())
                        .set(WangqiDocumentDO::getCurrentVersionedAt, dataObject.getCurrentVersionedAt())
                        .set(WangqiDocumentDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateStorageObjectId(WangqiDocumentId id, StorageObjectId storageObjectId) {
        return mapper.update(
                null,
                new LambdaUpdateWrapper<WangqiDocumentDO>()
                        .eq(WangqiDocumentDO::getId, WangqiDocumentIdCodec.toValue(id))
                        .set(WangqiDocumentDO::getStorageObjectId, StorageObjectIdCodec.toValue(storageObjectId)));
    }

    @Override
    public int updateVisibility(WangqiDocumentId id, String visibility) {
        return mapper.update(
                null,
                new LambdaUpdateWrapper<WangqiDocumentDO>()
                        .eq(WangqiDocumentDO::getId, WangqiDocumentIdCodec.toValue(id))
                        .set(WangqiDocumentDO::getVisibility, visibility));
    }

    @Override
    public int deleteByDocumentId(WangqiDocumentId id) {
        return eventMapper.delete(new LambdaQueryWrapper<WangqiDocumentEventDO>()
                .eq(WangqiDocumentEventDO::getDocumentId, WangqiDocumentIdCodec.toValue(id)));
    }

    @Override
    public int deleteById(WangqiDocumentId id) {
        return mapper.deleteById(WangqiDocumentIdCodec.toValue(id));
    }

    private static LambdaQueryWrapper<WangqiDocumentDO> buildWrapper(
            String keyword, String visibility, SortDirection sortDirection) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(visibility), WangqiDocumentDO::getVisibility, visibility)
                .and(StringUtils.isNotBlank(keyword), item -> item.like(WangqiDocumentDO::getTitle, keyword)
                        .or()
                        .like(WangqiDocumentDO::getSummary, keyword)
                        .or()
                        .like(WangqiDocumentDO::getContent, keyword))
                .orderBy(true, sortDirection != SortDirection.DESC, WangqiDocumentDO::getDocumentTime);
        return wrapper;
    }

    private void attachEvents(List<WangqiDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        List<WangqiDocumentId> ids = documents.stream()
                .filter(document -> document != null && document.getId() != null)
                .map(WangqiDocument::getId)
                .toList();
        Map<Long, List<WangqiDocumentEvent>> eventsByDocumentId = listEvents(ids).stream()
                .collect(Collectors.groupingBy(event -> WangqiDocumentIdCodec.toValue(event.getDocumentId())));
        for (WangqiDocument document : documents) {
            if (document == null || document.getId() == null) {
                continue;
            }
            document.setEvents(eventsByDocumentId.getOrDefault(
                    WangqiDocumentIdCodec.toValue(document.getId()), Collections.emptyList()));
        }
    }
}
