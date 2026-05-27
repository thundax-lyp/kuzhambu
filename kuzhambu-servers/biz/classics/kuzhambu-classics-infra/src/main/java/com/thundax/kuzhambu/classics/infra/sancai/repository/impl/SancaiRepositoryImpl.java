package com.thundax.kuzhambu.classics.infra.sancai.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiCategoryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiVolumeDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiCategoryMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiVolumeMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SancaiRepositoryImpl implements SancaiRepository {

    private final SancaiCategoryMapper categoryMapper;
    private final SancaiVolumeMapper volumeMapper;
    private final SancaiMapper entryMapper;

    public SancaiRepositoryImpl(
            SancaiCategoryMapper categoryMapper, SancaiVolumeMapper volumeMapper, SancaiMapper entryMapper) {
        this.categoryMapper = categoryMapper;
        this.volumeMapper = volumeMapper;
        this.entryMapper = entryMapper;
    }

    @Override
    public SancaiCategory getCategoryById(Long id) {
        return SancaiPersistenceAssembler.toCategoryDomain(categoryMapper.selectById(id));
    }

    @Override
    public List<SancaiCategory> listCategories(SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiCategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiCategoryDO::getPriority);
        return SancaiPersistenceAssembler.toCategoryDomainList(categoryMapper.selectList(wrapper));
    }

    @Override
    public SancaiVolume getVolumeById(Long id) {
        return SancaiPersistenceAssembler.toVolumeDomain(volumeMapper.selectById(id));
    }

    @Override
    public List<SancaiVolume> listVolumesByCategoryId(Long categoryId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiVolumeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, SancaiVolumeDO::getCategoryId, categoryId);
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiVolumeDO::getPriority);
        return SancaiPersistenceAssembler.toVolumeDomainList(volumeMapper.selectList(wrapper));
    }

    @Override
    public SancaiEntry getEntryById(Long id) {
        return SancaiPersistenceAssembler.toEntryDomain(entryMapper.selectById(id));
    }

    @Override
    public Page<SancaiEntry> pageEntries(
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
            int pageSize) {
        LambdaQueryWrapper<SancaiEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(volumeId != null, SancaiEntryDO::getVolumeId, volumeId)
                .eq(StringUtils.isNotBlank(lifecycleStatus), SancaiEntryDO::getLifecycleStatus, lifecycleStatus)
                .eq(StringUtils.isNotBlank(visibility), SancaiEntryDO::getVisibility, visibility)
                .eq(StringUtils.isNotBlank(translationStatus), SancaiEntryDO::getTranslationStatus, translationStatus)
                .eq(StringUtils.isNotBlank(imageStatus), SancaiEntryDO::getImageStatus, imageStatus)
                .eq(StringUtils.isNotBlank(visualAssetStatus), SancaiEntryDO::getVisualAssetStatus, visualAssetStatus)
                .eq(StringUtils.isNotBlank(refinementStatus), SancaiEntryDO::getRefinementStatus, refinementStatus)
                .and(StringUtils.isNotBlank(keyword), item -> item.like(SancaiEntryDO::getTitle, keyword)
                        .or()
                        .like(SancaiEntryDO::getOriginalText, keyword)
                        .or()
                        .like(SancaiEntryDO::getTranslationText, keyword))
                .orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryDO::getPriority);
        Page<SancaiEntryDO> dataPage = entryMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<SancaiEntry> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(SancaiPersistenceAssembler.toEntryDomainList(dataPage.getRecords()));
        return entityPage;
    }

    @Override
    public Long insertEntry(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        entryMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateEntry(SancaiEntry entry) {
        return entryMapper.updateById(SancaiPersistenceAssembler.toEntryObject(entry));
    }

    @Override
    public int updateEntryStatus(SancaiEntry entry) {
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, entry.getId())
                        .set(
                                SancaiEntryDO::getLifecycleStatus,
                                entry.getLifecycleStatus() == null
                                        ? null
                                        : entry.getLifecycleStatus().value()));
    }

    @Override
    public int updateEntryVisibility(Long id, String visibility) {
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, id)
                        .set(SancaiEntryDO::getVisibility, visibility));
    }

    @Override
    public int deleteEntryById(Long id) {
        return entryMapper.deleteById(id);
    }
}
