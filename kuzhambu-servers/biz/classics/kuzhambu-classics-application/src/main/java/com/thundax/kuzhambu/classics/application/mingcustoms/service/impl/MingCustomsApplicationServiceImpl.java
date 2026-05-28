package com.thundax.kuzhambu.classics.application.mingcustoms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsSaveCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MingCustomsApplicationServiceImpl implements MingCustomsApplicationService {

    private final MingCustomsRepository repository;

    public MingCustomsApplicationServiceImpl(MingCustomsRepository repository) {
        this.repository = repository;
    }

    @Override
    public MingCustomsEntry get(MingCustomsEntryId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<MingCustomsEntry> page(MingCustomsPageQuery query, PageQuery page) {
        IPage<MingCustomsEntry> dataPage = repository.page(
                query == null ? null : query.getCategory(),
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getTagName(),
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
    @Transactional(rollbackFor = Exception.class)
    public MingCustomsEntryId save(MingCustomsSaveCommand command) {
        MingCustomsEntry entry = toEntry(command);
        if (entry == null) {
            return null;
        }
        if (entry.getId() == null) {
            entry.setPriority(repository.maxPriority() + 1);
            return repository.insert(entry);
        }
        repository.update(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(MingCustomsEntryId id, String visibility) {
        repository.updateVisibility(id, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(MingCustomsEntryId id) {
        repository.deleteById(id);
    }

    @Override
    public List<MingCustomsKeyword> listKeywords(MingCustomsEntryId customId) {
        return repository.listKeywordsByCustomId(customId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MingCustomsKeywordId addKeyword(MingCustomsKeywordCommand command) {
        if (command == null) {
            return null;
        }
        MingCustomsKeyword keyword = new MingCustomsKeyword();
        keyword.setCustomId(command.getCustomId());
        keyword.setKeyword(command.getKeyword());
        keyword.setPriority(repository.maxPriority() + 1);
        return repository.insertKeyword(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortKeywords(MingCustomsKeywordSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<MingCustomsKeywordId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<MingCustomsKeyword> currentKeywords = repository.listKeywords(effectiveDirection);
        if (currentKeywords == null || currentKeywords.isEmpty() || currentKeywords.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentKeywords.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentKeywords.size());
        List<MingCustomsKeywordId> currentOrderedIds = new ArrayList<>(currentKeywords.size());
        for (int i = 0; i < currentKeywords.size(); i++) {
            MingCustomsKeyword keyword = currentKeywords.get(i);
            if (keyword == null || keyword.getId() == null) {
                throw sortDbFailure();
            }
            long keywordId = keyword.getId().value();
            indexById.put(keywordId, i);
            priorityById.put(keywordId, keyword.getPriority());
            currentOrderedIds.add(keyword.getId());
        }

        for (MingCustomsKeywordId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            MingCustomsKeywordId targetId = orderedIdList.get(i);
            MingCustomsKeywordId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updatePriorityOrThrow(targetId, temporaryPriority++);
            updatePriorityOrThrow(currentId, targetPriority);
            updatePriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKeyword(MingCustomsKeywordId id) {
        repository.deleteKeywordById(id);
    }

    @Override
    public List<String> listKeywordCloud(String visibility) {
        return repository.listKeywordCloud(visibility);
    }

    private void updatePriorityOrThrow(MingCustomsKeywordId id, int priority) {
        MingCustomsKeyword keyword = new MingCustomsKeyword();
        keyword.setId(id);
        keyword.setPriority(priority);
        if (repository.updateKeywordPriority(keyword) != 1) {
            throw sortDbFailure();
        }
    }

    private static BizException sortEmptyInput() {
        return new BizException(
                ErrorCode.SORT_EMPTY_INPUT.getCode(),
                ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                ErrorCode.SORT_EMPTY_INPUT.getMessage());
    }

    private static BizException sortMissingId() {
        return new BizException(
                ErrorCode.SORT_MISSING_ID.getCode(),
                ErrorCode.SORT_MISSING_ID.getMessageKey(),
                ErrorCode.SORT_MISSING_ID.getMessage());
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private static MingCustomsEntry toEntry(MingCustomsSaveCommand command) {
        if (command == null) {
            return null;
        }
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(command.getId());
        entry.setTitle(command.getTitle());
        entry.setCategory(command.getCategory());
        entry.setChapter(command.getChapter());
        entry.setSection(command.getSection());
        entry.setSummary(command.getSummary());
        entry.setContentFormat(command.getContentFormat());
        entry.setContent(command.getContent());
        entry.setOriginalExcerpts(command.getOriginalExcerpts());
        entry.setVisibility(command.getVisibility());
        return entry;
    }
}
