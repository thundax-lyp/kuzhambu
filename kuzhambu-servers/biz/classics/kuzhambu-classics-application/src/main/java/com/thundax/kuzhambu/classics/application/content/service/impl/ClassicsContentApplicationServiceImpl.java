package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentSnapshotAssembler;
import com.thundax.kuzhambu.classics.application.content.support.SancaiEntryVersionSnapshot;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.application.wangqi.support.WangqiDocumentVersionRestorer;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.content.service.ClassicsContentVersioningService;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsContentApplicationServiceImpl implements ClassicsContentApplicationService {

    private final ClassicsContentRepository repository;
    private final WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer;
    private final SancaiEntryVersionRestorer sancaiEntryVersionRestorer;
    private final SancaiAssetApplicationService sancaiAssetApplicationService;
    private final StorageApplicationService storageApplicationService;
    private final ClassicsContentVersioningService versioningService = new ClassicsContentVersioningService();
    private final ClassicsContentSnapshotAssembler snapshotAssembler = new ClassicsContentSnapshotAssembler();

    public ClassicsContentApplicationServiceImpl(
            ClassicsContentRepository repository, WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer) {
        this(repository, wangqiDocumentVersionRestorer, null, null, null);
    }

    public ClassicsContentApplicationServiceImpl(
            ClassicsContentRepository repository,
            WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer,
            SancaiEntryVersionRestorer sancaiEntryVersionRestorer) {
        this(repository, wangqiDocumentVersionRestorer, sancaiEntryVersionRestorer, null, null);
    }

    @Autowired
    public ClassicsContentApplicationServiceImpl(
            ClassicsContentRepository repository,
            WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer,
            SancaiEntryVersionRestorer sancaiEntryVersionRestorer,
            SancaiAssetApplicationService sancaiAssetApplicationService,
            StorageApplicationService storageApplicationService) {
        this.repository = repository;
        this.wangqiDocumentVersionRestorer = wangqiDocumentVersionRestorer;
        this.sancaiEntryVersionRestorer = sancaiEntryVersionRestorer;
        this.sancaiAssetApplicationService = sancaiAssetApplicationService;
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId) {
        return repository.listTags(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTags(ContentTagSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<ClassicsContentTagId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<ClassicsContentTag> currentTags = repository.listTags(effectiveDirection);
        if (currentTags == null || currentTags.isEmpty() || currentTags.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentTags.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentTags.size());
        List<ClassicsContentTagId> currentOrderedIds = new ArrayList<>(currentTags.size());
        for (int i = 0; i < currentTags.size(); i++) {
            ClassicsContentTag tag = currentTags.get(i);
            if (tag == null || tag.getId() == null) {
                throw sortDbFailure();
            }
            long tagId = tag.getId().value();
            indexById.put(tagId, i);
            priorityById.put(tagId, tag.getPriority());
            currentOrderedIds.add(tag.getId());
        }

        for (ClassicsContentTagId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxTagPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            ClassicsContentTagId targetId = orderedIdList.get(i);
            ClassicsContentTagId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateTagPriorityOrThrow(targetId, temporaryPriority++);
            updateTagPriorityOrThrow(currentId, targetPriority);
            updateTagPriorityOrThrow(targetId, currentPriority);

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
    public ClassicsContentTagId addTag(ContentTagCommand command) {
        ClassicsContentTag tag = command.toEntity();
        tag.setId(null);
        tag.setPriority(repository.maxTagPriority() + 1);
        return repository.insertTag(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentTagId updateTag(ContentTagCommand command) {
        ClassicsContentTag tag = command.toEntity();
        repository.updateTag(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(ClassicsContentTagId id) {
        repository.deleteTagById(id);
    }

    @Override
    public List<ClassicsContentQaPair> listQaPairs(String contentType, ClassicsContentId contentId) {
        return repository.listQaPairs(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId addQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        qaPair.setId(null);
        qaPair.setPriority(repository.maxQaPairPriority() + 1);
        return repository.insertQaPair(qaPair);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId updateQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        repository.updateQaPair(qaPair);
        return qaPair.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(ContentQaPairSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        sortQaPairs(command, repository.listQaPairs(effectiveDirection));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(String contentType, ClassicsContentId contentId, ContentQaPairSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        sortQaPairs(command, repository.listQaPairs(contentType, contentId, effectiveDirection));
    }

    private void sortQaPairs(ContentQaPairSortCommand command, List<ClassicsContentQaPair> currentQaPairs) {
        List<ClassicsContentQaPairId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        if (currentQaPairs == null || currentQaPairs.isEmpty() || currentQaPairs.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentQaPairs.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentQaPairs.size());
        List<ClassicsContentQaPairId> currentOrderedIds = new ArrayList<>(currentQaPairs.size());
        for (int i = 0; i < currentQaPairs.size(); i++) {
            ClassicsContentQaPair qaPair = currentQaPairs.get(i);
            if (qaPair == null || qaPair.getId() == null) {
                throw sortDbFailure();
            }
            long qaId = qaPair.getId().value();
            indexById.put(qaId, i);
            priorityById.put(qaId, qaPair.getPriority());
            currentOrderedIds.add(qaPair.getId());
        }

        for (ClassicsContentQaPairId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxQaPairPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            ClassicsContentQaPairId targetId = orderedIdList.get(i);
            ClassicsContentQaPairId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateQaPairPriorityOrThrow(targetId, temporaryPriority++);
            updateQaPairPriorityOrThrow(currentId, targetPriority);
            updateQaPairPriorityOrThrow(targetId, currentPriority);

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
    public void deleteQaPair(ClassicsContentQaPairId id) {
        repository.deleteQaPairById(id);
    }

    @Override
    public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
        return repository.listVersions(contentType, contentId);
    }

    @Override
    public ClassicsContentVersion getVersion(ClassicsContentVersionId id) {
        return repository.getVersionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVersions(String contentType, ClassicsContentId contentId) {
        return repository.deleteVersions(contentType, contentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion ensureVersioned(
            Versionable content, ClassicsContentChangeType changeType, String changeSummary) {
        if (content == null) {
            return null;
        }
        if (!versioningService.needsVersion(content)) {
            return repository.latestVersion(content.contentType(), content.contentId());
        }

        ClassicsContentVersion version = versioningService.newVersion(
                content,
                versioningService.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                new Date(),
                snapshotJson(content),
                changeType,
                changeSummary);
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningService.markVersioned(content, version);
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion applyAiResult(Versionable content, String changeSummary) {
        // TODO: Wire this to the future AI candidate confirmation flow before enabling AI_APPLIED versions.
        throw new BizException("AI 结果应用流程尚未接入 Classics Versionable");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion restoreHistoryVersion(ClassicsContentVersionId versionId) {
        ClassicsContentVersion version = repository.getVersionById(versionId);
        if (version == null) {
            throw new BizException("历史版本不存在");
        }
        if (version.getContentType() == ClassicsContentType.WANGQI_DOCUMENT) {
            Versionable restored = wangqiDocumentVersionRestorer.restoreSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            wangqiDocumentVersionRestorer.markVersioned((WangqiDocument) restored);
            return restoredVersion;
        }
        if (version.getContentType() == ClassicsContentType.SANCAI_ENTRY) {
            Versionable restored = sancaiEntryVersionRestorer.restoreSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            sancaiEntryVersionRestorer.markVersioned((SancaiEntry) restored);
            return restoredVersion;
        }
        throw new BizException("暂不支持恢复该类型历史版本: " + version.getContentType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentExportJobId createExportJob(ContentExportCommand command) {
        ClassicsContentExportJob job = command.toEntity();
        if (job.getRequestedAt() == null) {
            job.setRequestedAt(new Date());
        }
        return repository.insertExportJob(job);
    }

    @Override
    public PageResult<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, PageQuery page) {
        IPage<ClassicsContentExportJob> dataPage =
                repository.pageExportJobs(contentType, exportKind, status, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    private void updateTagPriorityOrThrow(ClassicsContentTagId id, int priority) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(id);
        tag.setPriority(priority);
        if (repository.updateTagPriority(tag) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateQaPairPriorityOrThrow(ClassicsContentQaPairId id, int priority) {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(id);
        qaPair.setPriority(priority);
        if (repository.updateQaPairPriority(qaPair) != 1) {
            throw sortDbFailure();
        }
    }

    private ClassicsContentVersion createRestoredVersion(Versionable content, ClassicsContentVersion restoredFrom) {
        ClassicsContentVersion version = versioningService.newVersion(
                content,
                versioningService.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                new Date(),
                snapshotJson(content),
                ClassicsContentChangeType.HISTORY_RESTORED,
                "恢复历史版本 v" + restoredFrom.getVersionNo());
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningService.markVersioned(content, version);
        return version;
    }

    private String snapshotJson(Versionable content) {
        if (content instanceof SancaiEntry entry && sancaiAssetApplicationService != null) {
            List<SancaiEntryImage> images = sancaiAssetApplicationService.listImages(entry.getId());
            if (storageApplicationService == null) {
                return snapshotAssembler.toSnapshotJson(entry, images);
            }
            return snapshotAssembler.toSnapshotJsonWithImageResources(
                    entry, images.stream().map(this::toImageResource).toList());
        }
        return snapshotAssembler.toSnapshotJson(content);
    }

    private SancaiEntryVersionSnapshot.ImageResource toImageResource(SancaiEntryImage image) {
        StoredObject storage = image == null || image.getStorageObjectId() == null
                ? null
                : storageApplicationService.get(
                        StoredObjectIdCodec.toDomain(StorageObjectIdCodec.toValue(image.getStorageObjectId())));
        return SancaiEntryVersionSnapshot.ImageResource.from(image, storage);
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
}
