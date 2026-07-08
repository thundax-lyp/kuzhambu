package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
import java.util.List;

public interface SancaiAssetRepository {

    SancaiEntryDraftId insertDraft(SancaiEntryDraft draft);

    SancaiEntryDraft getLatestDraftByEntryId(SancaiEntryId entryId);

    default List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {
        return List.of();
    }

    int deleteDraftByEntryId(SancaiEntryId entryId);

    default int deleteDraftById(SancaiEntryDraftId id) {
        return 0;
    }

    SancaiEntryImageId insertImage(SancaiEntryImage image);

    int updateImage(SancaiEntryImage image);

    int deleteImageById(SancaiEntryImageId id);

    SancaiEntryImage getImageById(SancaiEntryImageId id);

    List<SancaiEntryImage> listImages(SortDirection sortDirection);

    List<SancaiEntryImage> listImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection);

    List<SancaiEntryImage> listCurrentImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection);

    int clearCurrentImagesByEntryId(SancaiEntryId entryId);

    int markImageCurrent(SancaiEntryId entryId, SancaiEntryImageId imageId);

    int maxPriority();

    int updatePriority(SancaiEntryImage image);

    /**
     * 新增视觉资产版本记录，不负责变更当前使用版本。
     */
    SancaiVisualAssetId insertVisualAsset(SancaiVisualAsset visualAsset);

    /**
     * 更新既有视觉资产版本字段，不负责切换当前使用版本。
     */
    int updateVisualAsset(SancaiVisualAsset visualAsset);

    /**
     * 仅切换条目当前使用的视觉资产版本。
     */
    int updateCurrentVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    SancaiVisualAsset getVisualAssetById(SancaiVisualAssetId visualAssetId);

    /**
     * 按条目返回全部视觉资产版本，包含当前使用标记。
     */
    List<SancaiVisualAsset> listVisualAssetsByEntryId(SancaiEntryId entryId);

    int maxVisualAssetVersionNo(SancaiEntryId entryId);

    int updateVisualAssetImageAnalysisMarkdown(SancaiVisualAssetId visualAssetId, String imageAnalysisMarkdown);

    int updateVisualAssetFusionDescription(SancaiVisualAssetId visualAssetId, String fusionDescription);

    int updateVisualAssetVisualDescription(SancaiVisualAssetId visualAssetId, String visualDescription);

    SancaiShowcaseId insertShowcase(SancaiShowcase showcase);

    int updateShowcase(SancaiShowcase showcase);

    int markShowcaseCompleted(SancaiShowcaseId id, StorageObjectId storageObjectId, int entryCount);

    int markShowcaseCompleted(
            SancaiShowcaseId id,
            StorageObjectId storageObjectId,
            int entryCount,
            int assetCount,
            String filename,
            String contentType,
            Long sizeBytes,
            String sha256);

    int markShowcaseFailed(SancaiShowcaseId id);

    int markShowcaseFailed(SancaiShowcaseId id, String failureType, String failureMessage);

    int markShowcaseExpired(SancaiShowcaseId id);

    PageResult<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize);

    PageResult<SancaiShowcase> pageShowcases(
            String keyword,
            String status,
            String visibilityRiskStatus,
            Date requestedAtStart,
            Date requestedAtEnd,
            int pageNo,
            int pageSize);
}
