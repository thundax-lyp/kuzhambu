package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface SancaiRepository {

    SancaiCategory getCategoryById(SancaiCategoryId id);

    List<SancaiCategory> listCategories(SortDirection sortDirection);

    SancaiVolume getVolumeById(SancaiVolumeId id);

    List<SancaiVolume> listVolumesByCategoryId(SancaiCategoryId categoryId, SortDirection sortDirection);

    SancaiEntry getEntryById(SancaiEntryId id);

    Page<SancaiEntry> pageEntries(
            SancaiVolumeId volumeId,
            String keyword,
            String lifecycleStatus,
            String visibility,
            String translationStatus,
            String imageStatus,
            String visualAssetStatus,
            String refinementStatus,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    SancaiEntryId insertEntry(SancaiEntry entry);

    int updateEntry(SancaiEntry entry);

    int updateEntryStatus(SancaiEntry entry);

    int updateEntryVisibility(SancaiEntryId id, String visibility);

    int deleteEntryById(SancaiEntryId id);
}
