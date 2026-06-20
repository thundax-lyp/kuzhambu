package com.thundax.kuzhambu.classics.application.sharing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
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
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
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
public class ClassicsSharingApplicationServiceImpl implements ClassicsSharingApplicationService {

    private final ClassicsSharingRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final SancaiRepository sancaiRepository;
    private final WangqiDocumentRepository wangqiDocumentRepository;
    private final MingCustomsRepository mingCustomsRepository;
    private final ClassicsShareTokenGenerator shareTokenGenerator;
    private final ClassicsShareTokenHasher shareTokenHasher;
    private ClassicsShareProperties shareProperties = new ClassicsShareProperties();

    public ClassicsSharingApplicationServiceImpl(
            ClassicsSharingRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            SancaiRepository sancaiRepository,
            WangqiDocumentRepository wangqiDocumentRepository,
            MingCustomsRepository mingCustomsRepository,
            ClassicsShareTokenGenerator shareTokenGenerator,
            ClassicsShareTokenHasher shareTokenHasher) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.sancaiRepository = sancaiRepository;
        this.wangqiDocumentRepository = wangqiDocumentRepository;
        this.mingCustomsRepository = mingCustomsRepository;
        this.shareTokenGenerator = shareTokenGenerator;
        this.shareTokenHasher = shareTokenHasher;
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
        IPage<ClassicsShareLink> dataPage =
                repository.pageLinks(status, visibility, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    public PageResult<ClassicsSharePortalListItem> pagePortalShares(
            String contentType, String title, Date issuedAfter, Date issuedBefore, PageQuery page) {
        IPage<ClassicsSharePortalListItem> dataPage = repository.pagePortalShares(
                contentType, title, issuedAfter, issuedBefore, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareLinkCreateResult createLink(ShareLinkCreateCommand command) {
        String shareToken = shareTokenGenerator.generate();
        ClassicsShareLink link = command.toLink(shareTokenHasher.hash(shareToken));
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
            bindVersionSnapshot(target, link.getVisibility());
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
                listTargets(link.getId()));
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
        IPage<ClassicsShareAccessRecord> dataPage = repository.pageAccessRecords(
                query == null ? null : query.getShareLinkId(),
                query == null ? null : query.getShareTargetId(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
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
        Versionable content = loadContent(target);
        ClassicsSharedContentVisibility contentVisibility = visibilityOf(content);
        if (shareVisibility == ClassicsShareVisibility.PUBLIC
                && contentVisibility != ClassicsSharedContentVisibility.PUBLIC) {
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
