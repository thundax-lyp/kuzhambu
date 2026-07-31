package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategoryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class SancaiApplicationServiceImpl implements SancaiApplicationService {

    private final SancaiRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport;

    @Autowired
    public SancaiApplicationServiceImpl(
            SancaiRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.searchIndexSyncPublishSupport = searchIndexSyncPublishSupport;
    }

    @Override
    public List<SancaiCategory> listCategories() {
        return repository.listCategories(SortDirection.ASC);
    }

    @Override
    public List<SancaiCategoryOverview> listCategoryOverviews() {
        return repository.listCategoryOverviews(SortDirection.ASC);
    }

    @Override
    public SancaiCategory getCategory(SancaiCategoryId id) {
        return repository.getCategoryById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiCategoryId addCategory(SancaiCategoryCommand command) {
        SancaiCategory category = toNewCategory(command);
        category.setPriority(
                command.getPriority() == null ? repository.maxCategoryPriority() + 1 : command.getPriority());
        return repository.insertCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiCategoryId updateCategory(SancaiCategoryCommand command) {
        if (command == null || command.getId() == null) {
            throw invalidCategory("id");
        }
        SancaiCategory category = toNewCategory(command);
        category.setId(SancaiCategoryIdCodec.toDomain(command.getId()));
        SancaiCategory oldCategory = repository.getCategoryById(category.getId());
        if (oldCategory == null) {
            throw categoryNotFound();
        }
        category.setPriority(command.getPriority() == null ? oldCategory.getPriority() : command.getPriority());
        if (repository.updateCategory(category) != 1) {
            throw categoryNotFound();
        }
        return category.getId();
    }

    private static SancaiCategory toNewCategory(SancaiCategoryCommand command) {
        if (command == null || StringUtils.isBlank(command.getTitle())) {
            throw invalidCategory("title");
        }
        SancaiCategory category = new SancaiCategory();
        category.setTitle(command.getTitle().trim());
        category.setCategoryType(command.getCategoryType());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(SancaiCategoryId id) {
        if (id == null) {
            throw invalidCategory("id");
        }
        if (repository.countVolumesByCategoryId(id) > 0) {
            throw new BizException("三才图会门类下仍有关联卷，不能删除");
        }
        if (repository.deleteCategoryById(id) != 1) {
            throw categoryNotFound();
        }
    }

    @Override
    public List<SancaiVolume> listVolumes(SancaiCategoryId categoryId) {
        return repository.listVolumesByCategoryId(categoryId, SortDirection.ASC);
    }

    @Override
    public SancaiVolume getVolume(SancaiVolumeId id) {
        return id == null ? null : repository.getVolumeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVolumeId addVolume(SancaiVolumeCommand command) {
        SancaiVolume volume = toNewVolume(command);
        volume.setPriority(command.getPriority() == null ? repository.maxVolumePriority() + 1 : command.getPriority());
        return repository.insertVolume(volume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVolumeId updateVolume(SancaiVolumeCommand command) {
        if (command == null || command.getId() == null) {
            throw invalidVolume("id");
        }
        SancaiVolume volume = toNewVolume(command);
        volume.setId(SancaiVolumeIdCodec.toDomain(command.getId()));
        SancaiVolume oldVolume = repository.getVolumeById(volume.getId());
        if (oldVolume == null) {
            throw volumeNotFound();
        }
        volume.setPriority(command.getPriority() == null ? oldVolume.getPriority() : command.getPriority());
        if (repository.updateVolume(volume) != 1) {
            throw volumeNotFound();
        }
        return volume.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVolume(SancaiVolumeId id) {
        if (id == null) {
            throw invalidVolume("id");
        }
        if (repository.countEntriesByVolumeId(id) > 0) {
            throw new BizException("三才图会卷下仍有关联条目，不能删除");
        }
        if (repository.deleteVolumeById(id) != 1) {
            throw volumeNotFound();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortCategories(SancaiCategorySortCommand command) {
        List<SancaiCategoryId> orderedIdList = command == null ? null : command.getOrderedIds();
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                repository.listCategories(SortDirection.ASC),
                SancaiCategory::getId,
                SancaiCategoryId::value,
                SancaiCategory::getPriority,
                repository::maxCategoryPriority,
                this::updateCategoryPriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortVolumes(SancaiVolumeSortCommand command) {
        List<SancaiVolumeId> orderedIdList = command == null ? null : command.getOrderedIds();
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                repository.listVolumes(SortDirection.ASC),
                SancaiVolume::getId,
                SancaiVolumeId::value,
                SancaiVolume::getPriority,
                repository::maxVolumePriority,
                this::updateVolumePriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortEntries(SancaiEntrySortCommand command) {
        List<SancaiEntryId> orderedIdList = command == null ? null : command.getOrderedIds();
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                repository.listEntries(SortDirection.ASC),
                SancaiEntry::getId,
                SancaiEntryId::value,
                SancaiEntry::getPriority,
                repository::maxEntryPriority,
                this::updateEntryPriorityOrThrow);
    }

    @Override
    public SancaiEntry getEntry(SancaiEntryId id) {
        return id == null ? null : repository.getEntryById(id);
    }

    @Override
    public PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, List.of());
        }
        return repository.pageEntries(
                query == null ? null : SancaiCategoryIdCodec.toDomain(query.getCategoryId()),
                query == null ? null : SancaiVolumeIdCodec.toDomain(query.getVolumeId()),
                query == null ? null : query.getKeyword(),
                query == null || query.getLifecycleStatus() == null
                        ? null
                        : query.getLifecycleStatus().value(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null || query.getTranslationStatus() == null
                        ? null
                        : query.getTranslationStatus().value(),
                query == null || query.getImageStatus() == null
                        ? null
                        : query.getImageStatus().value(),
                query == null || query.getVisualAssetStatus() == null
                        ? null
                        : query.getVisualAssetStatus().value(),
                query == null || query.getRefinementStatus() == null
                        ? null
                        : query.getRefinementStatus().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    public List<SancaiEntry> listEntries(SancaiEntryPageQuery query) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return List.of();
        }
        return repository.listEntries(
                query == null ? null : SancaiCategoryIdCodec.toDomain(query.getCategoryId()),
                query == null ? null : SancaiVolumeIdCodec.toDomain(query.getVolumeId()),
                query == null ? null : query.getKeyword(),
                query == null || query.getLifecycleStatus() == null
                        ? null
                        : query.getLifecycleStatus().value(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null || query.getTranslationStatus() == null
                        ? null
                        : query.getTranslationStatus().value(),
                query == null || query.getImageStatus() == null
                        ? null
                        : query.getImageStatus().value(),
                query == null || query.getVisualAssetStatus() == null
                        ? null
                        : query.getVisualAssetStatus().value(),
                query == null || query.getRefinementStatus() == null
                        ? null
                        : query.getRefinementStatus().value(),
                query == null ? SortDirection.ASC : query.getSortDirection());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryId addEntry(SancaiEntryCommand command) {
        if (command == null) {
            return null;
        }
        requireExistingVolume(command.getVolumeId());
        SancaiEntry entry = toEntry(command);
        entry.setPriority(repository.maxEntryPriority() + 1);
        entry.setContentUpdatedAt(Instant.now());
        SancaiEntryId id = repository.insertEntry(entry);
        entry.setId(id);
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryId updateEntry(SancaiEntryCommand command) {
        if (command == null || command.getId() == null) {
            return null;
        }
        SancaiEntry currentEntry = repository.getEntryById(SancaiEntryIdCodec.toDomain(command.getId()));
        if (currentEntry == null) {
            throw new BizException("三才图会条目不存在");
        }
        SancaiVolumeId targetVolumeId = requireExistingVolume(command.getVolumeId());
        SancaiEntry entry = toEntry(command);
        boolean volumeChanged = !sameVolumeId(currentEntry.getVolumeId(), targetVolumeId);
        entry.setPriority(volumeChanged ? repository.maxEntryPriority() + 1 : currentEntry.getPriority());
        entry.setContentUpdatedAt(Instant.now());
        if (repository.updateEntry(entry) != 1) {
            throw new BizException("三才图会条目不存在");
        }
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEntryStatus(SancaiEntryStatusCommand command) {
        if (command == null || command.getId() == null) {
            return;
        }
        if (hasPermissionContext(command.getOperatorPermissions()) && !canEdit(command.getOperatorPermissions())) {
            throw permissionDenied();
        }
        SancaiEntry entry = repository.getEntryById(SancaiEntryIdCodec.toDomain(command.getId()));
        if (entry == null) {
            return;
        }
        SancaiEntryLifecycleStatus targetStatus = command.getLifecycleStatus();
        SancaiEntryLifecycleStatus currentStatus = entry.getLifecycleStatus();
        validateLifecycleChange(currentStatus, targetStatus);
        entry.setLifecycleStatus(targetStatus);
        entry.setContentUpdatedAt(Instant.now());
        markManualSaveVersion(entry, lifecycleChangeSummary(currentStatus, targetStatus));
        publishSearchSyncAfterCommit(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEntryVisibility(SancaiEntryId id, String visibility) {
        SancaiEntry entry = repository.getEntryById(id);
        if (entry == null) {
            return;
        }
        changeExistingEntryVisibility(entry, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeEntryVisibility(List<SancaiEntryId> ids, String visibility) {
        return batchChangeEntryVisibility(ids, visibility, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeEntryVisibility(
            List<SancaiEntryId> ids, String visibility, Set<String> operatorPermissions) {
        if (ids == null || ids.isEmpty()) {
            return ClassicsBatchOperationResult.empty();
        }
        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();
        for (SancaiEntryId id : ids) {
            Long contentId = id == null ? null : id.value();
            if (hasPermissionContext(operatorPermissions) && !canEdit(operatorPermissions)) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.SANCAI_ENTRY.value(), contentId, "PERMISSION_DENIED", "PERMISSION_DENIED"));
                continue;
            }
            try {
                SancaiEntry entry = id == null ? null : repository.getEntryById(id);
                if (entry == null) {
                    failures.add(ClassicsBatchOperationItemResult.failure(
                            ClassicsContentType.SANCAI_ENTRY.value(), contentId, "CONTENT_NOT_FOUND", "三才图会条目不存在"));
                    continue;
                }
                changeExistingEntryVisibility(entry, visibility);
                successes.add(ClassicsBatchOperationItemResult.success(
                        ClassicsContentType.SANCAI_ENTRY.value(),
                        contentId,
                        contentId,
                        entry.getVisibility().value()));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.SANCAI_ENTRY.value(),
                        contentId,
                        "BATCH_VISIBILITY_FAILED",
                        ex.getMessage()));
            }
        }
        return ClassicsBatchOperationResult.of(successes, failures);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntry(SancaiEntryId id) {
        SancaiEntry entry = repository.getEntryById(id);
        if (entry == null) {
            return;
        }
        entry.setContentUpdatedAt(Instant.now());
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动删除");
        publishDeleteAfterCommit(entry);
        repository.deleteEntryById(id);
    }

    private void updateCategoryPriorityOrThrow(SancaiCategoryId id, int priority) {
        SancaiCategory category = new SancaiCategory();
        category.setId(id);
        category.setPriority(priority);
        if (repository.updateCategoryPriority(category) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateVolumePriorityOrThrow(SancaiVolumeId id, int priority) {
        SancaiVolume volume = new SancaiVolume();
        volume.setId(id);
        volume.setPriority(priority);
        if (repository.updateVolumePriority(volume) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateEntryPriorityOrThrow(SancaiEntryId id, int priority) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(id);
        entry.setPriority(priority);
        if (repository.updateEntryPriority(entry) != 1) {
            throw sortDbFailure();
        }
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private static boolean hasPermissionContext(SancaiEntryPageQuery query) {
        return query != null && hasPermissionContext(query.getOperatorPermissions());
    }

    private static boolean hasPermissionContext(Set<String> operatorPermissions) {
        return operatorPermissions != null;
    }

    private static boolean canView(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canView(ClassicsContentType.SANCAI_ENTRY, operatorPermissions);
    }

    private static boolean canEdit(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canEdit(ClassicsContentType.SANCAI_ENTRY, operatorPermissions);
    }

    private static BizException permissionDenied() {
        return new BizException("PERMISSION_DENIED");
    }

    private static BizException invalidCategory(String field) {
        return new BizException("无效的三才图会门类参数: " + field);
    }

    private static BizException invalidVolume(String field) {
        return new BizException("无效的三才图会卷目参数: " + field);
    }

    private static BizException categoryNotFound() {
        return new BizException("三才图会门类不存在");
    }

    private static BizException volumeNotFound() {
        return new BizException("三才图会卷目不存在");
    }

    private SancaiVolume toNewVolume(SancaiVolumeCommand command) {
        if (command == null || command.getCategoryId() == null || StringUtils.isBlank(command.getTitle())) {
            throw invalidVolume("categoryId/title");
        }
        SancaiCategoryId categoryId = SancaiCategoryIdCodec.toDomain(command.getCategoryId());
        if (repository.getCategoryById(categoryId) == null) {
            throw categoryNotFound();
        }
        SancaiVolume volume = new SancaiVolume();
        volume.setCategoryId(categoryId);
        volume.setTitle(command.getTitle().trim());
        volume.setVolumeType(command.getVolumeType());
        return volume;
    }

    private SancaiVolumeId requireExistingVolume(Long volumeId) {
        if (volumeId == null) {
            throw invalidVolume("volumeId");
        }
        SancaiVolumeId targetVolumeId = SancaiVolumeIdCodec.toDomain(volumeId);
        if (repository.getVolumeById(targetVolumeId) == null) {
            throw volumeNotFound();
        }
        return targetVolumeId;
    }

    private static boolean sameVolumeId(SancaiVolumeId left, SancaiVolumeId right) {
        return left != null && left.equals(right);
    }

    private static SancaiEntry toEntry(SancaiEntryCommand command) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(command.getId()));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(command.getVolumeId()));
        entry.setTitle(command.getTitle());
        entry.setOriginalText(command.getOriginalText());
        entry.setTranslationText(command.getTranslationText());
        entry.setSummary(command.getSummary());
        entry.setLifecycleStatus(command.getLifecycleStatus());
        entry.setVisibility(command.getVisibility());
        entry.setTranslationStatus(command.getTranslationStatus());
        entry.setImageStatus(command.getImageStatus());
        entry.setVisualAssetStatus(command.getVisualAssetStatus());
        entry.setRefinementStatus(command.getRefinementStatus());
        return entry;
    }

    private void markManualSaveVersion(SancaiEntry entry) {
        markManualSaveVersion(entry, "手动保存");
    }

    private void markManualSaveVersion(SancaiEntry entry, String changeSummary) {
        contentApplicationService.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, changeSummary);
        repository.updateEntry(entry);
    }

    private void changeExistingEntryVisibility(SancaiEntry entry, String visibility) {
        entry.setVisibility(SancaiEntryVisibility.from(visibility));
        entry.setContentUpdatedAt(Instant.now());
        markManualSaveVersion(entry);
        publishSearchSyncAfterCommit(entry);
    }

    private void publishSearchSyncAfterCommit(SancaiEntry entry) {
        if (isPublicSearchEntry(entry)) {
            searchIndexSyncPublishSupport.publishUpsertAfterCommit(
                    ClassicsContentType.SANCAI_ENTRY,
                    String.valueOf(entry.getId().value()),
                    entry.getCurrentVersionNo());
            return;
        }
        publishDeleteAfterCommit(entry);
    }

    private void publishDeleteAfterCommit(SancaiEntry entry) {
        searchIndexSyncPublishSupport.publishDeleteAfterCommit(
                ClassicsContentType.SANCAI_ENTRY, String.valueOf(entry.getId().value()), entry.getCurrentVersionNo());
    }

    private boolean isPublicSearchEntry(SancaiEntry entry) {
        return entry != null
                && entry.getId() != null
                && entry.getCurrentVersionNo() != null
                && entry.getLifecycleStatus() == SancaiEntryLifecycleStatus.PUBLISHED
                && entry.getVisibility() == SancaiEntryVisibility.PUBLIC;
    }

    private static void validateLifecycleChange(
            SancaiEntryLifecycleStatus currentStatus, SancaiEntryLifecycleStatus targetStatus) {
        if (currentStatus == null) {
            throw new BizException("三才图会条目当前生命周期状态不能为空");
        }
        if (targetStatus == null) {
            throw new BizException("三才图会条目目标生命周期状态不能为空");
        }
        if (!currentStatus.canChangeTo(targetStatus)) {
            throw new BizException("三才图会条目生命周期不能从 " + currentStatus.value() + " 变更为 " + targetStatus.value());
        }
    }

    private static String lifecycleChangeSummary(
            SancaiEntryLifecycleStatus currentStatus, SancaiEntryLifecycleStatus targetStatus) {
        if (currentStatus == SancaiEntryLifecycleStatus.OFFLINE
                && targetStatus == SancaiEntryLifecycleStatus.PUBLISHED) {
            return "恢复发布条目";
        }
        if (targetStatus == SancaiEntryLifecycleStatus.OFFLINE) {
            return "下线条目";
        }
        return "发布条目";
    }
}
