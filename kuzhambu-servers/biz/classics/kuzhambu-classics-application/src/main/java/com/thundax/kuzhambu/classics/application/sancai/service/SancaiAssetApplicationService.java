package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftSaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
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
import java.util.List;

public interface SancaiAssetApplicationService {

    SancaiEntryDraftId saveDraft(SancaiDraftSaveCommand command);

    SancaiEntryDraft getLatestDraft(SancaiEntryId entryId);

    SancaiEntryImageId saveImage(SancaiImageCommand command);

    void sortImages(SancaiEntryImageSortCommand command);

    void deleteImage(SancaiEntryImageId id);

    List<SancaiEntryImage> listImages(SancaiEntryId entryId);

    SancaiVisualAssetId saveVisualAsset(SancaiVisualAsset visualAsset);

    void useVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    List<SancaiVisualAsset> listVisualAssets(SancaiEntryId entryId);

    SancaiShowcaseId requestShowcase(SancaiShowcaseCommand command);

    PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page);
}
