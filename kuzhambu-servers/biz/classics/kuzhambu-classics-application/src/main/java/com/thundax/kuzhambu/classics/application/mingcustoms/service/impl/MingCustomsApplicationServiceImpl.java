package com.thundax.kuzhambu.classics.application.mingcustoms.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MingCustomsApplicationServiceImpl implements MingCustomsApplicationService {

    private final MingCustomsRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport;
    private final ClassicsSharingApplicationService sharingApplicationService;

    public MingCustomsApplicationServiceImpl(
            MingCustomsRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport,
            ClassicsSharingApplicationService sharingApplicationService) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.searchIndexSyncPublishSupport = searchIndexSyncPublishSupport;
        this.sharingApplicationService = sharingApplicationService;
    }

    @Override
    public MingCustomsEntry get(MingCustomsEntryId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<MingCustomsEntry> page(MingCustomsPageQuery query, PageQuery page) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, List.of());
        }
        return repository.page(
                query == null ? null : query.getCategory(),
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getTagName(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MingCustomsEntryId add(MingCustomsCommand command) {
        MingCustomsEntry entry = toEntry(command);
        if (entry == null) {
            return null;
        }
        entry.setId(null);
        entry.setContentUpdatedAt(new Date());
        MingCustomsEntryId id = repository.insert(entry);
        entry.setId(id);
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MingCustomsEntryId update(MingCustomsCommand command) {
        MingCustomsEntry entry = toEntry(command);
        if (entry == null) {
            return null;
        }
        entry.setContentUpdatedAt(new Date());
        repository.update(entry);
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(MingCustomsEntryId id, String visibility) {
        changeVisibility(id, visibility, null);
    }

    void changeVisibility(MingCustomsEntryId id, String visibility, Set<String> operatorPermissions) {
        if (hasPermissionContext(operatorPermissions) && !canEdit(operatorPermissions)) {
            throw permissionDenied();
        }
        MingCustomsEntry entry = repository.getById(id);
        if (entry == null) {
            return;
        }
        changeExistingVisibility(entry, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeVisibility(List<MingCustomsEntryId> ids, String visibility) {
        return batchChangeVisibility(ids, visibility, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeVisibility(
            List<MingCustomsEntryId> ids, String visibility, Set<String> operatorPermissions) {
        if (ids == null || ids.isEmpty()) {
            return ClassicsBatchOperationResult.empty();
        }
        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();
        for (MingCustomsEntryId id : ids) {
            Long contentId = id == null ? null : id.value();
            if (hasPermissionContext(operatorPermissions) && !canEdit(operatorPermissions)) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.MING_CUSTOMS.value(), contentId, "PERMISSION_DENIED", "PERMISSION_DENIED"));
                continue;
            }
            try {
                MingCustomsEntry entry = id == null ? null : repository.getById(id);
                if (entry == null) {
                    failures.add(ClassicsBatchOperationItemResult.failure(
                            ClassicsContentType.MING_CUSTOMS.value(), contentId, "CONTENT_NOT_FOUND", "明代海关条目不存在"));
                    continue;
                }
                changeExistingVisibility(entry, visibility);
                successes.add(ClassicsBatchOperationItemResult.success(
                        ClassicsContentType.MING_CUSTOMS.value(),
                        contentId,
                        contentId,
                        entry.getVisibility().value()));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.MING_CUSTOMS.value(),
                        contentId,
                        "BATCH_VISIBILITY_FAILED",
                        ex.getMessage()));
            }
        }
        return ClassicsBatchOperationResult.of(successes, failures);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(MingCustomsEntryId id) {
        MingCustomsEntry entry = repository.getById(id);
        if (entry == null) {
            return;
        }
        entry.setContentUpdatedAt(new Date());
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动删除");
        if (sharingApplicationService != null) {
            sharingApplicationService.syncContentDeleted(ClassicsContentType.MING_CUSTOMS, id.value());
        }
        publishDeleteAfterCommit(entry);
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
    public List<MingCustomsKeywordCloudItem> listKeywordCloud(String visibility) {
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

    private static MingCustomsEntry toEntry(MingCustomsCommand command) {
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

    private void markManualSaveVersion(MingCustomsEntry entry) {
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");
        repository.update(entry);
    }

    private void changeExistingVisibility(MingCustomsEntry entry, String visibility) {
        entry.setVisibility(MingCustomsVisibility.from(visibility));
        entry.setContentUpdatedAt(new Date());
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
    }

    private void publishSearchSyncAfterCommit(MingCustomsEntry entry) {
        if (isPublicSearchEntry(entry)) {
            searchIndexSyncPublishSupport.publishUpsertAfterCommit(
                    ClassicsContentType.MING_CUSTOMS,
                    String.valueOf(entry.getId().value()),
                    entry.getCurrentVersionNo());
            return;
        }
        publishDeleteAfterCommit(entry);
    }

    private void publishDeleteAfterCommit(MingCustomsEntry entry) {
        if (entry == null || entry.getId() == null || entry.getCurrentVersionNo() == null) {
            return;
        }
        searchIndexSyncPublishSupport.publishDeleteAfterCommit(
                ClassicsContentType.MING_CUSTOMS, String.valueOf(entry.getId().value()), entry.getCurrentVersionNo());
    }

    private boolean isPublicSearchEntry(MingCustomsEntry entry) {
        return entry != null
                && entry.getId() != null
                && entry.getCurrentVersionNo() != null
                && entry.getVisibility() == MingCustomsVisibility.PUBLIC;
    }

    private static boolean hasPermissionContext(MingCustomsPageQuery query) {
        return query != null && hasPermissionContext(query.getOperatorPermissions());
    }

    private static boolean hasPermissionContext(Set<String> operatorPermissions) {
        return operatorPermissions != null;
    }

    private static boolean canView(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canView(ClassicsContentType.MING_CUSTOMS, operatorPermissions);
    }

    private static boolean canEdit(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canEdit(ClassicsContentType.MING_CUSTOMS, operatorPermissions);
    }

    private static BizException permissionDenied() {
        return new BizException("PERMISSION_DENIED");
    }
}
