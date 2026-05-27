package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface SancaiAssetRepository {

    Long insertDraft(SancaiEntryDraft draft);

    SancaiEntryDraft getLatestDraftByEntryId(Long entryId);

    int deleteDraftByEntryId(Long entryId);

    Long insertImage(SancaiEntryImage image);

    int updateImage(SancaiEntryImage image);

    int deleteImageById(Long id);

    List<SancaiEntryImage> listImagesByEntryId(Long entryId, SortDirection sortDirection);

    Long insertVisualAsset(SancaiVisualAsset visualAsset);

    int updateVisualAsset(SancaiVisualAsset visualAsset);

    int updateCurrentVisualAsset(Long entryId, Long visualAssetId);

    List<SancaiVisualAsset> listVisualAssetsByEntryId(Long entryId);

    Long insertShowcase(SancaiShowcase showcase);

    int updateShowcase(SancaiShowcase showcase);

    Page<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize);
}
