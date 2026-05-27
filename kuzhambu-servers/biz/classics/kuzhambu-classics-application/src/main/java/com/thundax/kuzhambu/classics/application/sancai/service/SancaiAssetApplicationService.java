package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftSaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface SancaiAssetApplicationService {

    Long saveDraft(SancaiDraftSaveCommand command);

    SancaiEntryDraft getLatestDraft(Long entryId);

    Long saveImage(SancaiImageCommand command);

    void deleteImage(Long id);

    List<SancaiEntryImage> listImages(Long entryId);

    Long saveVisualAsset(SancaiVisualAsset visualAsset);

    void useVisualAsset(Long entryId, Long visualAssetId);

    List<SancaiVisualAsset> listVisualAssets(Long entryId);

    Long requestShowcase(SancaiShowcaseCommand command);

    PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page);
}
