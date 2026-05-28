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

    List<SancaiCategory> listCategoriesByIds(List<Long> idList);

    SancaiCategory getCategoryById(SancaiCategoryId id);

    List<SancaiCategory> listCategories(SortDirection sortDirection);

    int maxCategoryPriority();

    SancaiVolume getVolumeById(SancaiVolumeId id);

    List<SancaiVolume> listVolumes(SortDirection sortDirection);

    List<SancaiVolume> listVolumesByCategoryId(SancaiCategoryId categoryId, SortDirection sortDirection);

    List<SancaiEntry> listEntries(SortDirection sortDirection);

    List<SancaiEntry> listEntriesByVolumeId(SancaiVolumeId volumeId, SortDirection sortDirection);

    int maxVolumePriority();

    int maxEntryPriority();

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

    int updateCategoryPriority(SancaiCategory category);

    int updateVolumePriority(SancaiVolume volume);

    int updateEntryPriority(SancaiEntry entry);
}
