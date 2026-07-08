package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiShowcaseJobResult;
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
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Date;
import java.util.List;

public interface SancaiAssetApplicationService {

    SancaiEntryDraftId updateDraft(SancaiDraftCommand command);

    SancaiEntryDraft getLatestDraft(SancaiEntryId entryId);

    SancaiEntryImageId updateImage(SancaiImageCommand command);

    SancaiEntryImage getImage(SancaiEntryImageId id);

    SancaiEntryImageResource uploadImage(SancaiEntryImageUploadCommand command);

    SancaiEntryImageContent getImageContent(SancaiEntryId entryId, SancaiEntryImageId imageId);

    ClassicsStoredContentResult getVisualAssetSourceContent(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    ClassicsStoredContentResult getVisualAssetGeneratedContent(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    void sortImages(SancaiEntryImageSortCommand command);

    void useImage(SancaiEntryId entryId, SancaiEntryImageId imageId);

    void deleteImage(SancaiEntryImageId id);

    List<SancaiEntryImage> listImages(SancaiEntryId entryId);

    /**
     * 保存视觉资产草稿字段，不隐式切换当前使用版本。
     * 当前使用版本切换必须通过 {@link #useVisualAsset(SancaiEntryId, SancaiVisualAssetId)} 单独执行。
     */
    SancaiVisualAssetId updateVisualAsset(SancaiVisualAsset visualAsset);

    /**
     * 将条目的当前视觉资产切换到指定版本。
     * 该操作不修改视觉资产本身的描述字段，仅更新条目和视觉资产之间的当前使用关系。
     */
    void useVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    /**
     * 返回条目下全部视觉资产版本，供管理端展示历史列表和当前使用状态。
     */
    List<SancaiVisualAsset> listVisualAssets(SancaiEntryId entryId);

    /**
     * 将已确认的信息融合结果写回到目标视觉资产，仅更新 fusionDescription。
     */
    void applyFusionDescription(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId, String fusionDescription);

    /**
     * 基于既有视觉资产生成新的生图版本，不隐式切换当前使用版本。
     */
    SancaiVisualAsset createGeneratedVisualAssetVersion(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId, StorageObjectId generatedImageStorageObjectId);

    SancaiShowcaseId requestShowcase(SancaiShowcaseCommand command);

    SancaiShowcaseJobResult requestShowcaseJob(SancaiShowcaseCommand command);

    ClassicsStoredContentResult getShowcaseContent(StorageObjectId storageObjectId);

    PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page);

    PageResult<SancaiShowcase> pageShowcases(
            String keyword,
            String status,
            String visibilityRiskStatus,
            Date requestedAtStart,
            Date requestedAtEnd,
            PageQuery page);
}
