package com.thundax.kuzhambu.classics.application.sharing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ClassicsShareTargetSortCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsSharingApplicationServiceImpl implements ClassicsSharingApplicationService {

    private final ClassicsSharingRepository repository;

    public ClassicsSharingApplicationServiceImpl(ClassicsSharingRepository repository) {
        this.repository = repository;
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
    @Transactional(rollbackFor = Exception.class)
    public ClassicsShareLinkId createLink(ShareLinkCreateCommand command) {
        ClassicsShareLink link = command.toLink();
        if (link.getIssuedAt() == null) {
            link.setIssuedAt(new Date());
        }
        ClassicsShareLinkId linkId = repository.insertLink(link);
        int nextPriority = repository.maxTargetPriority() + 1;
        List<ClassicsShareTarget> targets = command.getTargets() == null ? Collections.emptyList() : command.getTargets();
        for (ClassicsShareTarget target : targets) {
            target.setShareLinkId(linkId == null ? null : linkId);
            target.setPriority(nextPriority++);
            repository.insertTarget(target);
        }
        return linkId;
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
        return repository.listTargetsByLinkId(shareLinkId, SortDirection.ASC);
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
