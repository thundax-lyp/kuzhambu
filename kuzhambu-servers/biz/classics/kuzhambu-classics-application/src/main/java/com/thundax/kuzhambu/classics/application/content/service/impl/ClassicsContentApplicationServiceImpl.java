package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsContentApplicationServiceImpl implements ClassicsContentApplicationService {

    private final ClassicsContentRepository repository;

    public ClassicsContentApplicationServiceImpl(ClassicsContentRepository repository) {
        this.repository = repository;
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
    public ClassicsContentTagId saveTag(ContentTagCommand command) {
        ClassicsContentTag tag = command.toEntity();
        if (tag.getId() == null) {
            tag.setPriority(repository.maxTagPriority() + 1);
            return repository.insertTag(tag);
        }
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
    public ClassicsContentQaPairId saveQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        if (qaPair.getId() == null) {
            qaPair.setPriority(repository.maxQaPairPriority() + 1);
            return repository.insertQaPair(qaPair);
        }
        repository.updateQaPair(qaPair);
        return qaPair.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(ContentQaPairSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<ClassicsContentQaPairId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<ClassicsContentQaPair> currentQaPairs = repository.listQaPairs(effectiveDirection);
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
