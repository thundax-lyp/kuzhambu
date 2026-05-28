package com.thundax.kuzhambu.classics.application.wangqi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSaveCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class WangqiDocumentApplicationServiceImpl implements WangqiDocumentApplicationService {

    private final WangqiDocumentRepository repository;

    public WangqiDocumentApplicationServiceImpl(WangqiDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public WangqiDocument get(WangqiDocumentId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<WangqiDocument> page(WangqiDocumentPageQuery query, PageQuery page) {
        IPage<WangqiDocument> dataPage = repository.page(
                query == null ? null : query.getKeyword(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    public List<WangqiDocument> listTimeline(WangqiDocumentPageQuery query) {
        return repository.listTimeline(
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentId save(WangqiDocumentSaveCommand command) {
        WangqiDocument document = toDocument(command);
        if (document.getId() == null) {
            return repository.insert(document);
        }
        repository.update(document);
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStorageObject(WangqiDocumentId id, StorageObjectId storageObjectId) {
        repository.updateStorageObjectId(id, storageObjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(WangqiDocumentVisibilityCommand command) {
        repository.updateVisibility(
                WangqiDocumentIdCodec.toDomain(command.getId()),
                command.getVisibility().value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(WangqiDocumentId id) {
        repository.deleteById(id);
    }

    private static WangqiDocument toDocument(WangqiDocumentSaveCommand command) {
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(command.getId()));
        document.setTitle(command.getTitle());
        document.setSummary(command.getSummary());
        document.setContentFormat(command.getContentFormat());
        document.setContent(command.getContent());
        document.setDocumentTime(command.getDocumentTime());
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(command.getStorageObjectId()));
        document.setVisibility(command.getVisibility());
        return document;
    }
}
