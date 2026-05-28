package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface SancaiAssetRepository {

    SancaiEntryDraftId insertDraft(SancaiEntryDraft draft);

    SancaiEntryDraft getLatestDraftByEntryId(SancaiEntryId entryId);

    int deleteDraftByEntryId(SancaiEntryId entryId);

    SancaiEntryImageId insertImage(SancaiEntryImage image);

    int updateImage(SancaiEntryImage image);

    int deleteImageById(SancaiEntryImageId id);

    SancaiEntryImage getImageById(SancaiEntryImageId id);

    List<SancaiEntryImage> listImages(SortDirection sortDirection);

    List<SancaiEntryImage> listImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection);

    int maxPriority();

    int updatePriority(SancaiEntryImage image);

    SancaiVisualAssetId insertVisualAsset(SancaiVisualAsset visualAsset);

    int updateVisualAsset(SancaiVisualAsset visualAsset);

    int updateCurrentVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId);

    List<SancaiVisualAsset> listVisualAssetsByEntryId(SancaiEntryId entryId);

    SancaiShowcaseId insertShowcase(SancaiShowcase showcase);

    int updateShowcase(SancaiShowcase showcase);

    Page<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize);
}
