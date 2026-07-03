package com.thundax.kuzhambu.classics.application.sharing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sharing.command.BatchShareCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ClassicsShareTargetSortCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.configure.ClassicsShareProperties;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.application.sharing.support.ClassicsShareTokenGenerator;
import com.thundax.kuzhambu.classics.application.sharing.support.ClassicsShareTokenHasher;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsSharingApplicationServiceImpl implements ClassicsSharingApplicationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ClassicsSharingRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final SancaiRepository sancaiRepository;
    private final WangqiDocumentRepository wangqiDocumentRepository;
    private final MingCustomsRepository mingCustomsRepository;
    private final ClassicsShareTokenGenerator shareTokenGenerator;
    private final ClassicsShareTokenHasher shareTokenHasher;
    private final StorageFacade storageFacade;
    private ClassicsShareProperties shareProperties = new ClassicsShareProperties();

    public ClassicsSharingApplicationServiceImpl(
            ClassicsSharingRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            SancaiRepository sancaiRepository,
            WangqiDocumentRepository wangqiDocumentRepository,
            MingCustomsRepository mingCustomsRepository,
            ClassicsShareTokenGenerator shareTokenGenerator,
            ClassicsShareTokenHasher shareTokenHasher,
            StorageFacade storageFacade) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.sancaiRepository = sancaiRepository;
        this.wangqiDocumentRepository = wangqiDocumentRepository;
        this.mingCustomsRepository = mingCustomsRepository;
        this.shareTokenGenerator = shareTokenGenerator;
        this.shareTokenHasher = shareTokenHasher;
        this.storageFacade = storageFacade;
    }

    @Autowired(required = false)
    public void setShareProperties(ClassicsShareProperties shareProperties) {
        if (shareProperties != null) {
            this.shareProperties = shareProperties;
        }
    }

    @Override
    public ClassicsShareLink getLink(ClassicsShareLinkId id) {
        return id == null ? null : repository.getLinkById(id);
    }

    @Override
    public ClassicsShareLink getLinkByTokenHash(String tokenHash) {
        return repository.getLinkByTokenHash(tokenHash);
    }

    @Override
    public PageResult<ClassicsShareLink> pageLinks(String status, String visibility, PageQuery page) {
        return repository.pageLinks(status, visibility, page.getPageNo(), page.getPageSize());
    }

    @Override
    public PageResult<ClassicsSharePortalListItem> pagePortalShares(
            String contentType, String title, Date issuedAfter, Date issuedBefore, PageQuery page) {
        return repository.pagePortalShares(
                contentType, title, issuedAfter, issuedBefore, page.getPageNo(), page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareLinkCreateResult createLink(ShareLinkCreateCommand command) {
        return createLink(command, false);
    }

    private ShareLinkCreateResult createLink(ShareLinkCreateCommand command, boolean allowPrivateContent) {
        String shareToken = shareTokenGenerator.generate();
        ClassicsShareLink link = command.toLink(shareToken, shareTokenHasher.hash(shareToken));
        if (link.getIssuedAt() == null) {
            link.setIssuedAt(new Date());
        }
        ClassicsShareLinkId linkId = repository.insertLink(link);
        int nextPriority = repository.maxTargetPriority() + 1;
        List<ShareTargetCreateCommand> targetCommands =
                command.getTargets() == null ? Collections.emptyList() : command.getTargets();
        List<ClassicsShareTarget> savedTargets = new ArrayList<>(targetCommands.size());
        for (ShareTargetCreateCommand targetCommand : targetCommands) {
            ClassicsShareTarget target = targetCommand.toTarget();
            bindVersionSnapshot(target, link.getVisibility(), allowPrivateContent);
            target.setShareLinkId(linkId == null ? null : linkId);
            target.setPriority(nextPriority++);
            repository.insertTarget(target);
            savedTargets.add(target);
        }
        return new ShareLinkCreateResult(
                linkId,
                shareToken,
                shareProperties.buildShareUrl(shareToken),
                link.getTitle(),
                link.getVisibility(),
                link.getStatus(),
                link.getExpiresAt(),
                savedTargets);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchCreateLinks(BatchShareCreateCommand command) {
        if (command == null
                || command.getTargets() == null
                || command.getTargets().isEmpty()) {
            return ClassicsBatchOperationResult.empty();
        }
        if (!command.isPrivateContentConfirmed()) {
            ensureNoPrivateBatchTarget(command.getTargets());
        }
        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();
        Set<String> targetKeys = new HashSet<>();
        for (ShareTargetCreateCommand target : command.getTargets()) {
            String targetKey = targetKey(target);
            String contentType = target == null || target.getContentType() == null
                    ? null
                    : target.getContentType().value();
            Long contentId = target == null || target.getContentId() == null
                    ? null
                    : target.getContentId().value();
            if (targetKey == null || !targetKeys.add(targetKey)) {
                failures.add(
                        ClassicsBatchOperationItemResult.failure(contentType, contentId, "DUPLICATE_TARGET", "重复分享目标"));
                continue;
            }
            try {
                ShareLinkCreateResult result = createLink(
                        new ShareLinkCreateCommand(
                                batchTitle(command.getTitlePrefix(), target),
                                command.getVisibility(),
                                command.getStatus(),
                                command.getVisibilityRiskStatus(),
                                null,
                                command.getExpiresAt(),
                                List.of(target)),
                        command.isPrivateContentConfirmed());
                successes.add(ClassicsBatchOperationItemResult.success(
                        contentType,
                        contentId,
                        result.getId() == null ? null : result.getId().value(),
                        result.getStatus() == null ? null : result.getStatus().value()));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        contentType, contentId, "BATCH_SHARE_FAILED", ex.getMessage()));
            }
        }
        return ClassicsBatchOperationResult.of(successes, failures);
    }

    @Override
    public SharePortalResult getPortalShare(String shareToken) {
        ClassicsShareLink link = repository.getLinkByTokenHash(shareTokenHasher.hash(shareToken));
        if (!isPortalVisible(link)) {
            throw shareContentNotFound();
        }
        return new SharePortalResult(
                link.getTitle(),
                link.getVisibility(),
                link.getStatus(),
                link.getIssuedAt(),
                link.getExpiresAt(),
                enrichPortalTargets(listTargets(link.getId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsStoredContentResult getPortalShareResourceContent(
            String shareToken, Long storageObjectId, boolean download) {
        if (storageObjectId == null || storageFacade == null) {
            throw shareContentNotFound();
        }
        ClassicsShareLink link = repository.getLinkByTokenHash(shareTokenHasher.hash(shareToken));
        if (!isPortalVisible(link)) {
            throw shareContentNotFound();
        }
        ClassicsShareTarget matchedTarget = findReadableResourceTarget(link.getId(), storageObjectId, download);
        if (matchedTarget == null) {
            throw shareContentNotFound();
        }
        OpenStorageFacadeRequest request = OpenStorageFacadeRequest.builder()
                .storageObjectId(storageObjectId)
                .build();
        if (!storageFacade.exists(request)) {
            throw shareContentNotFound();
        }
        ClassicsStoredContentResult content = toStoredContentResult(storageFacade.open(request));
        if (content == null) {
            throw shareContentNotFound();
        }
        recordAllowedResourceAccess(link.getId(), matchedTarget.getId(), storageObjectId);
        return content;
    }

    private static boolean isPortalVisible(ClassicsShareLink link) {
        if (link == null || link.getId() == null) {
            return false;
        }
        if (link.getVisibility() != ClassicsShareVisibility.PUBLIC
                || link.getStatus() != ClassicsShareLinkStatus.ACTIVE) {
            return false;
        }
        return link.getExpiresAt() == null || link.getExpiresAt().after(new Date());
    }

    private List<ClassicsShareTarget> enrichPortalTargets(List<ClassicsShareTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return targets.stream().map(this::enrichPortalTarget).toList();
    }

    private ClassicsShareTarget enrichPortalTarget(ClassicsShareTarget target) {
        if (target == null || target.getContentType() != ClassicsContentType.WANGQI_DOCUMENT) {
            return target;
        }
        JsonNode snapshot = readSnapshot(target.getContentSnapshotJson());
        Long storageObjectId = longValue(snapshot == null ? null : snapshot.get("storageObjectId"));
        if (storageObjectId == null || snapshot == null) {
            return target;
        }
        StorageObjectFacadeDto storedObject = openStoredObject(storageObjectId);
        if (storedObject == null) {
            return target;
        }
        ObjectNode snapshotNode = snapshot.deepCopy();
        snapshotNode.put("originalFilename", storedObject.getOriginalFilename());
        snapshotNode.put("contentType", storedObject.getContentType());
        if (storedObject.getSize() != null) {
            snapshotNode.put("size", storedObject.getSize());
        } else {
            snapshotNode.putNull("size");
        }
        return new ClassicsShareTarget(
                target.getId(),
                target.getShareLinkId(),
                target.getContentType(),
                target.getContentId(),
                target.getContentVersionId(),
                target.getContentVersionNo(),
                target.getTitleSnapshot(),
                snapshotNode.toString(),
                target.getContentVisibilitySnapshot(),
                target.getTargetStatus(),
                target.getPriority(),
                target.getCurrentContentVersionId(),
                target.getCurrentContentVersionNo(),
                target.getContentChangedAfterShare());
    }

    private StorageObjectFacadeDto openStoredObject(Long storageObjectId) {
        if (storageFacade == null || storageObjectId == null) {
            return null;
        }
        OpenStorageFacadeResponse response = storageFacade.open(OpenStorageFacadeRequest.builder()
                .storageObjectId(storageObjectId)
                .build());
        if (response == null || response.getStoredObject() == null) {
            return null;
        }
        if (response.getInputStream() != null) {
            try {
                response.getInputStream().close();
            } catch (java.io.IOException ignored) {
            }
        }
        return response.getStoredObject();
    }

    private static ClassicsStoredContentResult toStoredContentResult(OpenStorageFacadeResponse response) {
        if (response == null || response.getStoredObject() == null || response.getInputStream() == null) {
            return null;
        }
        StorageObjectFacadeDto storedObject = response.getStoredObject();
        return new ClassicsStoredContentResult(
                storedObject.getId(),
                storedObject.getOriginalFilename(),
                storedObject.getContentType(),
                storedObject.getSize(),
                response.getInputStream());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ShareLinkStatusCommand command) {
        repository.updateLinkStatus(command.getId(), command.getStatus().value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTargets(ClassicsShareTargetSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<ClassicsShareTargetId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<ClassicsShareTarget> currentTargets = repository.listTargets(effectiveDirection);
        if (currentTargets == null || currentTargets.isEmpty() || currentTargets.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentTargets.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentTargets.size());
        List<ClassicsShareTargetId> currentOrderedIds = new ArrayList<>(currentTargets.size());
        for (int i = 0; i < currentTargets.size(); i++) {
            ClassicsShareTarget target = currentTargets.get(i);
            if (target == null || target.getId() == null) {
                throw sortDbFailure();
            }
            long targetId = target.getId().value();
            indexById.put(targetId, i);
            priorityById.put(targetId, target.getPriority());
            currentOrderedIds.add(target.getId());
        }

        for (ClassicsShareTargetId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxTargetPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            ClassicsShareTargetId targetId = orderedIdList.get(i);
            ClassicsShareTargetId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateTargetPriorityOrThrow(targetId, temporaryPriority++);
            updateTargetPriorityOrThrow(currentId, targetPriority);
            updateTargetPriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    public List<ClassicsShareTarget> listTargets(ClassicsShareLinkId shareLinkId) {
        List<ClassicsShareTarget> targets = repository.listTargetsByLinkId(shareLinkId, SortDirection.ASC);
        if (targets == null) {
            return List.of();
        }
        targets.forEach(this::enrichCurrentVersionMarker);
        return targets;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAccess(ClassicsShareAccessRecord record) {
        if (record.getAccessedAt() == null) {
            record.setAccessedAt(new Date());
        }
        repository.insertAccessRecord(record);
        repository.increaseAccessCount(record.getShareLinkId());
    }

    @Override
    public PageResult<ClassicsShareAccessRecord> pageAccessRecords(ShareAccessQuery query, PageQuery page) {
        return repository.pageAccessRecords(
                query == null ? null : query.getShareLinkId(),
                query == null ? null : query.getShareTargetId(),
                page.getPageNo(),
                page.getPageSize());
    }

    private ClassicsShareTarget findReadableResourceTarget(
            ClassicsShareLinkId shareLinkId, Long storageObjectId, boolean download) {
        List<ClassicsShareTarget> targets = repository.listTargetsByLinkId(shareLinkId, SortDirection.ASC);
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        for (ClassicsShareTarget target : targets) {
            if (!isAvailableTarget(target)) {
                continue;
            }
            if (snapshotContainsReadableResource(target, storageObjectId, download)) {
                return target;
            }
        }
        return null;
    }

    private static boolean snapshotContainsReadableResource(
            ClassicsShareTarget target, Long storageObjectId, boolean download) {
        JsonNode snapshot = readSnapshot(target.getContentSnapshotJson());
        if (snapshot == null || target.getContentType() == null) {
            return false;
        }
        return switch (target.getContentType()) {
            case WANGQI_DOCUMENT ->
                longValue(snapshot.get("storageObjectId")) != null
                        && longValue(snapshot.get("storageObjectId")).equals(storageObjectId);
            case SANCAI_ENTRY -> !download && sancaiSnapshotContainsResource(snapshot, storageObjectId);
            case MING_CUSTOMS -> false;
        };
    }

    private static boolean sancaiSnapshotContainsResource(JsonNode snapshot, Long storageObjectId) {
        JsonNode images = snapshot.get("images");
        if (images == null || !images.isArray()) {
            return false;
        }
        for (JsonNode image : images) {
            Long imageStorageObjectId = longValue(image.get("storageObjectId"));
            if (storageObjectId.equals(imageStorageObjectId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAvailableTarget(ClassicsShareTarget target) {
        return target != null
                && target.getId() != null
                && target.getTargetStatus() == ClassicsShareTargetStatus.AVAILABLE;
    }

    private void recordAllowedResourceAccess(
            ClassicsShareLinkId shareLinkId, ClassicsShareTargetId shareTargetId, Long storageObjectId) {
        ClassicsShareAccessRecord record = new ClassicsShareAccessRecord();
        record.setShareLinkId(shareLinkId);
        record.setShareTargetId(shareTargetId);
        record.setAccessResult(ClassicsShareAccessResult.ALLOWED);
        record.setClientSnapshot("resourceStorageObjectId=" + storageObjectId);
        recordAccess(record);
    }

    private void updateTargetPriorityOrThrow(ClassicsShareTargetId id, int priority) {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setId(id);
        target.setPriority(priority);
        if (repository.updateTargetPriority(target) != 1) {
            throw sortDbFailure();
        }
    }

    private void bindVersionSnapshot(ClassicsShareTarget target, ClassicsShareVisibility shareVisibility) {
        bindVersionSnapshot(target, shareVisibility, false);
    }

    private void bindVersionSnapshot(
            ClassicsShareTarget target, ClassicsShareVisibility shareVisibility, boolean allowPrivateContent) {
        Versionable content = loadContent(target);
        ClassicsSharedContentVisibility contentVisibility = visibilityOf(content);
        if (shareVisibility == ClassicsShareVisibility.PUBLIC
                && contentVisibility != ClassicsSharedContentVisibility.PUBLIC
                && !allowPrivateContent) {
            throw privateContentCannotBePublicShared();
        }

        ClassicsContentVersion version =
                contentApplicationService.ensureVersioned(content, ClassicsContentChangeType.SHARE_CREATED, "创建分享");
        if (version == null || version.getId() == null) {
            throw shareContentNotFound();
        }

        target.setContentVersionId(version.getId());
        target.setContentVersionNo(version.getVersionNo());
        target.setContentSnapshotJson(version.getSnapshotJson());
        target.setTitleSnapshot(titleOf(content));
        target.setContentVisibilitySnapshot(contentVisibility);
        persistVersionMarker(content);
    }

    private void ensureNoPrivateBatchTarget(List<ShareTargetCreateCommand> targets) {
        for (ShareTargetCreateCommand target : targets) {
            if (target == null || target.getContentType() == null || target.getContentId() == null) {
                continue;
            }
            Versionable content =
                    loadContent(target.getContentType(), target.getContentId().value());
            if (content != null && visibilityOf(content) != ClassicsSharedContentVisibility.PUBLIC) {
                throw privateContentCannotBePublicShared();
            }
        }
    }

    private String batchTitle(String titlePrefix, ShareTargetCreateCommand target) {
        Versionable content = target == null || target.getContentType() == null || target.getContentId() == null
                ? null
                : loadContent(target.getContentType(), target.getContentId().value());
        String title = content == null ? "分享" : titleOf(content);
        if (titlePrefix == null || titlePrefix.isBlank()) {
            return title;
        }
        return titlePrefix + title;
    }

    private static String targetKey(ShareTargetCreateCommand target) {
        if (target == null || target.getContentType() == null || target.getContentId() == null) {
            return null;
        }
        return target.getContentType().value() + ":" + target.getContentId().value();
    }

    private Versionable loadContent(ClassicsShareTarget target) {
        if (target == null || target.getContentType() == null || target.getContentId() == null) {
            throw shareContentNotFound();
        }
        ClassicsContentType contentType = target.getContentType();
        Long contentId = target.getContentId().value();
        Versionable content = loadContent(contentType, contentId);
        if (content == null) {
            throw shareContentNotFound();
        }
        return content;
    }

    private Versionable loadContent(ClassicsContentType contentType, Long contentId) {
        return switch (contentType) {
            case SANCAI_ENTRY -> sancaiRepository.getEntryById(SancaiEntryIdCodec.toDomain(contentId));
            case WANGQI_DOCUMENT -> wangqiDocumentRepository.getById(WangqiDocumentIdCodec.toDomain(contentId));
            case MING_CUSTOMS -> mingCustomsRepository.getById(MingCustomsEntryIdCodec.toDomain(contentId));
        };
    }

    private void enrichCurrentVersionMarker(ClassicsShareTarget target) {
        if (target == null || target.getContentType() == null || target.getContentId() == null) {
            return;
        }
        Versionable content =
                loadContent(target.getContentType(), target.getContentId().value());
        if (content == null) {
            target.setContentChangedAfterShare(Boolean.TRUE);
            return;
        }
        target.setCurrentContentVersionId(content.currentVersionId());
        target.setCurrentContentVersionNo(content.currentVersionNo());
        target.setContentChangedAfterShare(target.getContentVersionId() != null
                && !target.getContentVersionId().equals(content.currentVersionId()));
    }

    private void persistVersionMarker(Versionable content) {
        int updated =
                switch (content.contentType()) {
                    case SANCAI_ENTRY -> sancaiRepository.updateEntry((SancaiEntry) content);
                    case WANGQI_DOCUMENT -> wangqiDocumentRepository.update((WangqiDocument) content);
                    case MING_CUSTOMS -> mingCustomsRepository.update((MingCustomsEntry) content);
                };
        if (updated != 1) {
            throw shareContentNotFound();
        }
    }

    private static String titleOf(Versionable content) {
        return switch (content.contentType()) {
            case SANCAI_ENTRY -> ((SancaiEntry) content).getTitle();
            case WANGQI_DOCUMENT -> ((WangqiDocument) content).getTitle();
            case MING_CUSTOMS -> ((MingCustomsEntry) content).getTitle();
        };
    }

    private static ClassicsSharedContentVisibility visibilityOf(Versionable content) {
        return switch (content.contentType()) {
            case SANCAI_ENTRY ->
                ((SancaiEntry) content).getVisibility() == null
                        ? null
                        : ClassicsSharedContentVisibility.from(
                                ((SancaiEntry) content).getVisibility().value());
            case WANGQI_DOCUMENT ->
                ((WangqiDocument) content).getVisibility() == null
                        ? null
                        : ClassicsSharedContentVisibility.from(
                                ((WangqiDocument) content).getVisibility().value());
            case MING_CUSTOMS ->
                ((MingCustomsEntry) content).getVisibility() == null
                        ? null
                        : ClassicsSharedContentVisibility.from(
                                ((MingCustomsEntry) content).getVisibility().value());
        };
    }

    private static JsonNode readSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(snapshotJson);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private static Long longValue(JsonNode node) {
        return node == null || !node.canConvertToLong() ? null : node.asLong();
    }

    private static BizException shareContentNotFound() {
        return new BizException("分享内容不存在或不支持版本标定");
    }

    private static BizException privateContentCannotBePublicShared() {
        return new BizException("私有古籍内容不允许公开分享");
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
