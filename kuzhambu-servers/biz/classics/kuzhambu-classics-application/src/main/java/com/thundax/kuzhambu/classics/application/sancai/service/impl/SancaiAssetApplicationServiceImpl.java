package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftSaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
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
public class SancaiAssetApplicationServiceImpl implements SancaiAssetApplicationService {

    private final SancaiAssetRepository repository;

    public SancaiAssetApplicationServiceImpl(SancaiAssetRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryDraftId saveDraft(SancaiDraftSaveCommand command) {
        SancaiEntryDraft draft = new SancaiEntryDraft();
        draft.setEntryId(SancaiEntryIdCodec.toDomain(command.getEntryId()));
        draft.setAutosavedAt(command.getAutosavedAt() == null ? new Date() : command.getAutosavedAt());
        draft.setDraftJson(command.getDraftJson());
        return repository.insertDraft(draft);
    }

    @Override
    public SancaiEntryDraft getLatestDraft(SancaiEntryId entryId) {
        return repository.getLatestDraftByEntryId(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryImageId saveImage(SancaiImageCommand command) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageIdCodec.toDomain(command.getId()));
        image.setEntryId(SancaiEntryIdCodec.toDomain(command.getEntryId()));
        image.setStorageObjectId(command.getStorageObjectId());
        image.setImageType(command.getImageType());
        image.setTitle(command.getTitle());
        image.setCurrentUsed(command.isCurrentUsed());
        if (image.getId() == null) {
            image.setPriority(repository.maxPriority() + 1);
            return repository.insertImage(image);
        }
        repository.updateImage(image);
        return image.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortImages(SancaiEntryImageSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SancaiEntryImageId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<SancaiEntryImage> currentImages = repository.listImages(effectiveDirection);
        if (currentImages == null || currentImages.isEmpty() || currentImages.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentImages.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentImages.size());
        List<SancaiEntryImageId> currentOrderedIds = new ArrayList<>(currentImages.size());
        for (int i = 0; i < currentImages.size(); i++) {
            SancaiEntryImage image = currentImages.get(i);
            if (image == null || image.getId() == null) {
                throw sortDbFailure();
            }
            long imageId = image.getId().value();
            indexById.put(imageId, i);
            priorityById.put(imageId, image.getPriority());
            currentOrderedIds.add(image.getId());
        }

        for (SancaiEntryImageId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SancaiEntryImageId targetId = orderedIdList.get(i);
            SancaiEntryImageId currentId = currentOrderedIds.get(i);
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
    public void deleteImage(SancaiEntryImageId id) {
        repository.deleteImageById(id);
    }

    @Override
    public List<SancaiEntryImage> listImages(SancaiEntryId entryId) {
        return repository.listImagesByEntryId(entryId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVisualAssetId saveVisualAsset(SancaiVisualAsset visualAsset) {
        if (visualAsset.getId() == null) {
            return repository.insertVisualAsset(visualAsset);
        }
        repository.updateVisualAsset(visualAsset);
        return visualAsset.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        repository.updateCurrentVisualAsset(entryId, visualAssetId);
    }

    @Override
    public List<SancaiVisualAsset> listVisualAssets(SancaiEntryId entryId) {
        return repository.listVisualAssetsByEntryId(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiShowcaseId requestShowcase(SancaiShowcaseCommand command) {
        SancaiShowcase showcase = new SancaiShowcase();
        showcase.setRequestedAt(command.getRequestedAt() == null ? new Date() : command.getRequestedAt());
        showcase.setStatus(command.getStatus());
        showcase.setScopeJson(command.getScopeJson());
        showcase.setStorageObjectId(command.getStorageObjectId());
        showcase.setEntryCount(command.getEntryCount());
        showcase.setVisibilityRiskStatus(command.getVisibilityRiskStatus());
        return repository.insertShowcase(showcase);
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page) {
        IPage<SancaiShowcase> dataPage = repository.pageShowcases(status, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    private void updatePriorityOrThrow(SancaiEntryImageId id, int priority) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(id);
        image.setPriority(priority);
        if (repository.updatePriority(image) != 1) {
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
