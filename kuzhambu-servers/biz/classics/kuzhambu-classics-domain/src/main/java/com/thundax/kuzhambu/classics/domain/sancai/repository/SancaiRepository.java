package com.thundax.kuzhambu.classics.domain.sancai.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface SancaiRepository {

    SancaiCategory getCategoryById(Long id);

    List<SancaiCategory> listCategories(SortDirection sortDirection);

    SancaiVolume getVolumeById(Long id);

    List<SancaiVolume> listVolumesByCategoryId(Long categoryId, SortDirection sortDirection);

    SancaiEntry getEntryById(Long id);

    Page<SancaiEntry> pageEntries(
            Long volumeId,
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

    Long insertEntry(SancaiEntry entry);

    int updateEntry(SancaiEntry entry);

    int updateEntryStatus(SancaiEntry entry);

    int updateEntryVisibility(Long id, String visibility);

    int deleteEntryById(Long id);
}
