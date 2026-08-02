package com.thundax.kuzhambu.classics.application.sancai.support;

import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public abstract class FakeSancaiRepositorySupport implements SancaiRepository {

    @Override
    public List<SancaiCategory> listCategoriesByIds(List<Long> idList) {
        return List.of();
    }

    @Override
    public SancaiCategory getCategoryById(SancaiCategoryId id) {
        return null;
    }

    @Override
    public List<SancaiCategory> listCategories(SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public List<SancaiCategoryOverview> listCategoryOverviews(SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public List<SancaiCategoryOverview> listCategoryRepresentativeOverviewsByEntryIds(
            List<Long> entryIds, SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public int maxCategoryPriority() {
        return 0;
    }

    @Override
    public SancaiCategoryId insertCategory(SancaiCategory category) {
        return null;
    }

    @Override
    public int updateCategory(SancaiCategory category) {
        return 0;
    }

    @Override
    public int countVolumesByCategoryId(SancaiCategoryId categoryId) {
        return 0;
    }

    @Override
    public int deleteCategoryById(SancaiCategoryId id) {
        return 0;
    }

    @Override
    public SancaiVolume getVolumeById(SancaiVolumeId id) {
        return null;
    }

    @Override
    public List<SancaiVolume> listVolumes(SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public List<SancaiVolume> listVolumesByCategoryId(SancaiCategoryId categoryId, SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public SancaiVolumeId insertVolume(SancaiVolume volume) {
        return null;
    }

    @Override
    public int updateVolume(SancaiVolume volume) {
        return 0;
    }

    @Override
    public int countEntriesByVolumeId(SancaiVolumeId volumeId) {
        return 0;
    }

    @Override
    public int deleteVolumeById(SancaiVolumeId id) {
        return 0;
    }

    @Override
    public List<SancaiEntry> listEntries(SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public List<SancaiEntry> listEntriesByVolumeId(SancaiVolumeId volumeId, SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public List<SancaiEntry> listEntriesByIds(List<SancaiEntryId> ids) {
        return List.of();
    }

    @Override
    public int maxVolumePriority() {
        return 0;
    }

    @Override
    public int maxEntryPriority() {
        return 0;
    }

    @Override
    public int maxEntryPriorityByVolumeId(SancaiVolumeId volumeId) {
        return 0;
    }

    @Override
    public SancaiEntry getEntryById(SancaiEntryId id) {
        return null;
    }

    @Override
    public PageResult<SancaiEntry> pageEntries(
            SancaiCategoryId categoryId,
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
            int pageSize) {
        return new PageResult<>();
    }

    @Override
    public List<SancaiEntry> listEntries(
            SancaiCategoryId categoryId,
            SancaiVolumeId volumeId,
            String keyword,
            String lifecycleStatus,
            String visibility,
            String translationStatus,
            String imageStatus,
            String visualAssetStatus,
            String refinementStatus,
            SortDirection sortDirection) {
        return List.of();
    }

    @Override
    public SancaiEntryId insertEntry(SancaiEntry entry) {
        return null;
    }

    @Override
    public int updateEntry(SancaiEntry entry) {
        return 0;
    }

    @Override
    public int updateRestoredEntry(SancaiEntry entry) {
        return 0;
    }

    @Override
    public int updateEntryStatus(SancaiEntry entry) {
        return 0;
    }

    @Override
    public int updateEntryVisibility(SancaiEntryId id, String visibility) {
        return 0;
    }

    @Override
    public int deleteEntryById(SancaiEntryId id) {
        return 0;
    }

    @Override
    public int updateCategoryPriority(SancaiCategory category) {
        return 0;
    }

    @Override
    public int updateVolumePriority(SancaiVolume volume) {
        return 0;
    }

    @Override
    public int updateEntryPriority(SancaiEntry entry) {
        return 0;
    }
}
