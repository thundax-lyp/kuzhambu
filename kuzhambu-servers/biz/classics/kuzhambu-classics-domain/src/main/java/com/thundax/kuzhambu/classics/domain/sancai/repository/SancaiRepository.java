package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface SancaiRepository {

    List<SancaiCategory> listCategoriesByIdList(List<Long> idList);

    SancaiCategory getByCategoryId(SancaiCategoryId id);

    List<SancaiCategory> listCategories(SortDirection sortDirection);

    List<SancaiCategoryOverview> listCategoryOverviews(SortDirection sortDirection);

    List<SancaiCategoryOverview> listCategoryRepresentativeOverviewsByEntryIds(
            List<Long> entryIds, SortDirection sortDirection);

    int maxCategoryPriority();

    SancaiCategoryId insertCategory(SancaiCategory category);

    int updateCategory(SancaiCategory category);

    int countVolumesByCategoryId(SancaiCategoryId categoryId);

    int deleteByCategoryId(SancaiCategoryId id);

    SancaiVolume getByVolumeId(SancaiVolumeId id);

    List<SancaiVolume> listVolumes(SortDirection sortDirection);

    List<SancaiVolume> listVolumesByCategoryId(SancaiCategoryId categoryId, SortDirection sortDirection);

    SancaiVolumeId insertVolume(SancaiVolume volume);

    int updateVolume(SancaiVolume volume);

    int countEntriesByVolumeId(SancaiVolumeId volumeId);

    int deleteByVolumeId(SancaiVolumeId id);

    List<SancaiEntry> listEntries(SortDirection sortDirection);

    List<SancaiEntry> listEntriesByVolumeId(SancaiVolumeId volumeId, SortDirection sortDirection);

    List<SancaiEntry> listEntriesByIdList(List<SancaiEntryId> ids);

    int maxVolumePriority();

    int maxEntryPriority();

    int maxEntryPriorityByVolumeId(SancaiVolumeId volumeId);

    SancaiEntry getByEntryId(SancaiEntryId id);

    PageResult<SancaiEntry> page(
            SancaiCategoryId categoryId,
            SancaiVolumeId volumeId,
            String keyword,
            String lifecycleStatus,
            String translationStatus,
            String imageStatus,
            String visualAssetStatus,
            String refinementStatus,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    List<SancaiEntry> listEntries(
            SancaiCategoryId categoryId,
            SancaiVolumeId volumeId,
            String keyword,
            String lifecycleStatus,
            String translationStatus,
            String imageStatus,
            String visualAssetStatus,
            String refinementStatus,
            SortDirection sortDirection);

    SancaiEntryId insertEntry(SancaiEntry entry);

    int updateEntry(SancaiEntry entry);

    int updateRestoredEntry(SancaiEntry entry);

    int updateEntryStatus(SancaiEntry entry);

    int deleteByEntryId(SancaiEntryId id);

    int updateCategoryPriority(SancaiCategory category);

    int updateVolumePriority(SancaiVolume volume);

    int updateEntryPriority(SancaiEntry entry);
}
