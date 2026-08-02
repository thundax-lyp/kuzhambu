package com.thundax.kuzhambu.classics.application.mingcustoms.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MingCustomsApplicationServiceImpl implements MingCustomsApplicationService {

    private final MingCustomsRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final ClassicsPublicationWriteGuard publicationWriteGuard;

    @Autowired
    public MingCustomsApplicationServiceImpl(
            MingCustomsRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            ClassicsPublicationWriteGuard publicationWriteGuard) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.publicationWriteGuard = publicationWriteGuard;
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
                query == null ? null : query.getTagId(),
                query == null ? null : query.getTagNameSnapshot(),
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
        entry.setContentUpdatedAt(Instant.now());
        MingCustomsEntryId id = repository.insert(entry);
        entry.setId(id);
        markManualSaveVersion(entry);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MingCustomsEntryId update(MingCustomsCommand command) {
        MingCustomsEntry entry = toEntry(command);
        if (entry == null) {
            return null;
        }
        requireWritable(entry.getId(), ClassicsPublicationWriteOperation.EDIT);
        MingCustomsEntry current = requireEntry(entry.getId());
        preservePublicationState(entry, current);
        entry.setContentUpdatedAt(Instant.now());
        repository.update(entry);
        markManualSaveVersion(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(MingCustomsEntryId id) {
        publicationWriteGuard.prepareDeletion(
                ClassicsContentType.MING_CUSTOMS, new ClassicsContentId(id == null ? null : id.value()));
        MingCustomsEntry entry = repository.getById(id);
        if (entry == null) {
            return;
        }
        entry.setContentUpdatedAt(Instant.now());
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动删除");
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
        requireWritable(command.getCustomId(), ClassicsPublicationWriteOperation.EDIT);
        MingCustomsKeyword keyword = new MingCustomsKeyword();
        keyword.setCustomId(command.getCustomId());
        keyword.setKeyword(command.getKeyword());
        keyword.setPriority(repository.maxPriority() + 1);
        MingCustomsKeywordId keywordId = repository.insertKeyword(keyword);
        return keywordId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortKeywords(MingCustomsKeywordSortCommand command) {
        List<MingCustomsKeywordId> orderedIdList = command == null ? null : command.getOrderedIds();
        List<MingCustomsKeyword> keywords = repository.listKeywords(SortDirection.ASC);
        keywords.stream()
                .map(MingCustomsKeyword::getCustomId)
                .distinct()
                .forEach(id -> requireWritable(id, ClassicsPublicationWriteOperation.EDIT));
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                keywords,
                MingCustomsKeyword::getId,
                MingCustomsKeywordId::value,
                MingCustomsKeyword::getPriority,
                repository::maxPriority,
                this::updatePriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKeyword(MingCustomsKeywordId id) {
        MingCustomsKeyword keyword = findKeyword(id);
        if (keyword != null) {
            requireWritable(keyword.getCustomId(), ClassicsPublicationWriteOperation.EDIT);
        }
        repository.deleteKeywordById(id);
    }

    @Override
    public List<MingCustomsKeywordCloudItem> listKeywordCloud() {
        return repository.listKeywordCloud();
    }

    @Override
    public List<MingCustomsTagCloudItem> listTagCloud(MingCustomsPageQuery query) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return List.of();
        }
        return repository.listTagCloud(
                query == null ? null : query.getCategory(), query == null ? null : query.getKeyword());
    }

    private void updatePriorityOrThrow(MingCustomsKeywordId id, int priority) {
        MingCustomsKeyword keyword = new MingCustomsKeyword();
        keyword.setId(id);
        keyword.setPriority(priority);
        if (repository.updateKeywordPriority(keyword) != 1) {
            throw sortDbFailure();
        }
    }

    private void requireWritable(MingCustomsEntryId id, ClassicsPublicationWriteOperation operation) {
        publicationWriteGuard.requireWritable(
                ClassicsContentType.MING_CUSTOMS, new ClassicsContentId(id == null ? null : id.value()), operation);
    }

    private MingCustomsKeyword findKeyword(MingCustomsKeywordId id) {
        if (id == null) {
            return null;
        }
        List<MingCustomsKeyword> keywords = repository.listKeywords(SortDirection.ASC);
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        return keywords.stream()
                .filter(keyword -> keyword != null && id.equals(keyword.getId()))
                .findFirst()
                .orElse(null);
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
        return entry;
    }

    private static void preservePublicationState(MingCustomsEntry entry, MingCustomsEntry current) {
        entry.setLifecycleStatus(current.getLifecycleStatus());
        entry.setTransitionStatus(current.getTransitionStatus());
        entry.setCurrentPublicationJobId(current.getCurrentPublicationJobId());
        entry.setCurrentVersionId(current.getCurrentVersionId());
        entry.setCurrentVersionNo(current.getCurrentVersionNo());
        entry.setCurrentVersionedAt(current.getCurrentVersionedAt());
    }

    private void markManualSaveVersion(MingCustomsEntry entry) {
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");
        repository.update(entry);
    }

    private MingCustomsEntry requireEntry(MingCustomsEntryId id) {
        MingCustomsEntry entry = repository.getById(id);
        if (entry == null) {
            throw new BizException("明代海关条目不存在");
        }
        return entry;
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
}
