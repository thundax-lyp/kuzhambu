package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftSaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
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
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
import java.util.List;
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
        image.setPriority(command.getPriority());
        if (image.getId() == null) {
            return repository.insertImage(image);
        }
        repository.updateImage(image);
        return image.getId();
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
}
