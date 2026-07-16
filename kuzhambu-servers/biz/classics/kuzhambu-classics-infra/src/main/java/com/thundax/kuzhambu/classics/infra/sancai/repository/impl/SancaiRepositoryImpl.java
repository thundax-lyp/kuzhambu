package com.thundax.kuzhambu.classics.infra.sancai.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiCategoryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiVolumeDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiCategoryMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiVolumeMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public List<SancaiCategory> listCategoriesByIds(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return List.of();
        }
        return SancaiPersistenceAssembler.toCategoryDomainList(categoryMapper.selectBatchIds(idList));
    }

    @Override
    public SancaiCategory getCategoryById(SancaiCategoryId id) {
        return SancaiPersistenceAssembler.toCategoryDomain(categoryMapper.selectById(id == null ? null : id.value()));
    }

    @Override
    public List<SancaiCategory> listCategories(SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiCategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiCategoryDO::getPriority);
        return SancaiPersistenceAssembler.toCategoryDomainList(categoryMapper.selectList(wrapper));
    }

    @Override
    public List<SancaiCategoryOverview> listCategoryOverviews(SortDirection sortDirection) {
        QueryWrapper<SancaiCategoryDO> wrapper = new QueryWrapper<>();
        wrapper.select(
                        "id AS categoryId",
                        "COALESCE((SELECT COUNT(1) FROM classics_sancai_entry e"
                                + " JOIN classics_sancai_volume v ON e.volume_id = v.id"
                                + " WHERE v.category_id = classics_sancai_category.id"
                                + " AND e.lifecycle_status = 'PUBLISHED' AND e.visibility = 'PUBLIC'), 0)"
                                + " AS publicEntryCount",
                        "COALESCE((SELECT COUNT(DISTINCT e.id) FROM classics_sancai_entry e"
                                + " JOIN classics_sancai_volume v ON e.volume_id = v.id"
                                + " JOIN classics_sancai_entry_image i ON i.entry_id = e.id"
                                + " WHERE v.category_id = classics_sancai_category.id"
                                + " AND e.lifecycle_status = 'PUBLISHED' AND e.visibility = 'PUBLIC'), 0)"
                                + " AS illustratedEntryCount",
                        "(SELECT e.id FROM classics_sancai_entry e"
                                + " JOIN classics_sancai_volume v ON e.volume_id = v.id"
                                + " JOIN classics_sancai_entry_image i ON i.entry_id = e.id"
                                + " WHERE v.category_id = classics_sancai_category.id"
                                + " AND e.lifecycle_status = 'PUBLISHED' AND e.visibility = 'PUBLIC'"
                                + " ORDER BY i.current_used DESC, e.priority ASC, i.priority ASC LIMIT 1)"
                                + " AS representativeEntryId",
                        "(SELECT i.id FROM classics_sancai_entry e"
                                + " JOIN classics_sancai_volume v ON e.volume_id = v.id"
                                + " JOIN classics_sancai_entry_image i ON i.entry_id = e.id"
                                + " WHERE v.category_id = classics_sancai_category.id"
                                + " AND e.lifecycle_status = 'PUBLISHED' AND e.visibility = 'PUBLIC'"
                                + " ORDER BY i.current_used DESC, e.priority ASC, i.priority ASC LIMIT 1)"
                                + " AS representativeImageId",
                        "(SELECT COALESCE(i.title, e.title) FROM classics_sancai_entry e"
                                + " JOIN classics_sancai_volume v ON e.volume_id = v.id"
                                + " JOIN classics_sancai_entry_image i ON i.entry_id = e.id"
                                + " WHERE v.category_id = classics_sancai_category.id"
                                + " AND e.lifecycle_status = 'PUBLISHED' AND e.visibility = 'PUBLIC'"
                                + " ORDER BY i.current_used DESC, e.priority ASC, i.priority ASC LIMIT 1)"
                                + " AS representativeImageTitle")
                .orderBy(true, sortDirection != SortDirection.DESC, "priority");
        return categoryMapper.selectMaps(wrapper).stream()
                .map(SancaiRepositoryImpl::toCategoryOverview)
                .toList();
    }

    @Override
    public int maxCategoryPriority() {
        return maxPriority(categoryMapper.selectObjs(new QueryWrapper<SancaiCategoryDO>().select("max(priority)")));
    }

    @Override
    public SancaiCategoryId insertCategory(SancaiCategory category) {
        SancaiCategoryDO dataObject = SancaiPersistenceAssembler.toCategoryObject(category);
        categoryMapper.insert(dataObject);
        return SancaiCategoryIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateCategory(SancaiCategory category) {
        SancaiCategoryDO dataObject = SancaiPersistenceAssembler.toCategoryObject(category);
        return categoryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiCategoryDO>()
                        .eq(SancaiCategoryDO::getId, dataObject.getId())
                        .set(SancaiCategoryDO::getTitle, dataObject.getTitle())
                        .set(SancaiCategoryDO::getCategoryType, dataObject.getCategoryType())
                        .set(SancaiCategoryDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int countVolumesByCategoryId(SancaiCategoryId categoryId) {
        return Math.toIntExact(volumeMapper.selectCount(new LambdaQueryWrapper<SancaiVolumeDO>()
                .eq(SancaiVolumeDO::getCategoryId, SancaiCategoryIdCodec.toValue(categoryId))));
    }

    @Override
    public int deleteCategoryById(SancaiCategoryId id) {
        return categoryMapper.deleteById(SancaiCategoryIdCodec.toValue(id));
    }

    @Override
    public SancaiVolume getVolumeById(SancaiVolumeId id) {
        return SancaiPersistenceAssembler.toVolumeDomain(volumeMapper.selectById(SancaiVolumeIdCodec.toValue(id)));
    }

    @Override
    public List<SancaiVolume> listVolumes(SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiVolumeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiVolumeDO::getPriority);
        return SancaiPersistenceAssembler.toVolumeDomainList(volumeMapper.selectList(wrapper));
    }

    @Override
    public List<SancaiVolume> listVolumesByCategoryId(SancaiCategoryId categoryId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiVolumeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, SancaiVolumeDO::getCategoryId, SancaiCategoryIdCodec.toValue(categoryId));
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiVolumeDO::getPriority);
        return SancaiPersistenceAssembler.toVolumeDomainList(volumeMapper.selectList(wrapper));
    }

    @Override
    public SancaiVolumeId insertVolume(SancaiVolume volume) {
        SancaiVolumeDO dataObject = SancaiPersistenceAssembler.toVolumeObject(volume);
        volumeMapper.insert(dataObject);
        return SancaiVolumeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateVolume(SancaiVolume volume) {
        SancaiVolumeDO dataObject = SancaiPersistenceAssembler.toVolumeObject(volume);
        return volumeMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVolumeDO>()
                        .eq(SancaiVolumeDO::getId, dataObject.getId())
                        .set(SancaiVolumeDO::getCategoryId, dataObject.getCategoryId())
                        .set(SancaiVolumeDO::getTitle, dataObject.getTitle())
                        .set(SancaiVolumeDO::getVolumeType, dataObject.getVolumeType())
                        .set(SancaiVolumeDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int countEntriesByVolumeId(SancaiVolumeId volumeId) {
        return Math.toIntExact(entryMapper.selectCount(new LambdaQueryWrapper<SancaiEntryDO>()
                .eq(SancaiEntryDO::getVolumeId, SancaiVolumeIdCodec.toValue(volumeId))));
    }

    @Override
    public int deleteVolumeById(SancaiVolumeId id) {
        return volumeMapper.deleteById(SancaiVolumeIdCodec.toValue(id));
    }

    @Override
    public List<SancaiEntry> listEntries(SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryDO::getPriority);
        return SancaiPersistenceAssembler.toEntryDomainList(entryMapper.selectList(wrapper));
    }

    @Override
    public List<SancaiEntry> listEntriesByVolumeId(SancaiVolumeId volumeId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(volumeId != null, SancaiEntryDO::getVolumeId, SancaiVolumeIdCodec.toValue(volumeId));
        wrapper.orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryDO::getPriority);
        return SancaiPersistenceAssembler.toEntryDomainList(entryMapper.selectList(wrapper));
    }

    @Override
    public int maxVolumePriority() {
        return maxPriority(volumeMapper.selectObjs(new QueryWrapper<SancaiVolumeDO>().select("max(priority)")));
    }

    @Override
    public int maxEntryPriority() {
        return maxPriority(entryMapper.selectObjs(new QueryWrapper<SancaiEntryDO>().select("max(priority)")));
    }

    @Override
    public int maxEntryPriorityByVolumeId(SancaiVolumeId volumeId) {
        return maxPriority(entryMapper.selectObjs(new QueryWrapper<SancaiEntryDO>()
                .select("max(priority)")
                .eq("volume_id", SancaiVolumeIdCodec.toValue(volumeId))));
    }

    @Override
    public SancaiEntry getEntryById(SancaiEntryId id) {
        return SancaiPersistenceAssembler.toEntryDomain(entryMapper.selectById(SancaiEntryIdCodec.toValue(id)));
    }

    @Override
    public PageResult<SancaiEntry> pageEntries(
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
        LambdaQueryWrapper<SancaiEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(volumeId != null, SancaiEntryDO::getVolumeId, SancaiVolumeIdCodec.toValue(volumeId))
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
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                SancaiPersistenceAssembler.toEntryDomainList(dataPage.getRecords()));
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
        List<Long> categoryVolumeIds = listVolumeIdsByCategory(categoryId);
        if (categoryId != null && categoryVolumeIds.isEmpty()) {
            return List.of();
        }
        if (categoryId != null
                && volumeId != null
                && !categoryVolumeIds.contains(SancaiVolumeIdCodec.toValue(volumeId))) {
            return List.of();
        }

        LambdaQueryWrapper<SancaiEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(volumeId != null, SancaiEntryDO::getVolumeId, SancaiVolumeIdCodec.toValue(volumeId))
                .in(volumeId == null && categoryId != null, SancaiEntryDO::getVolumeId, categoryVolumeIds)
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
        return SancaiPersistenceAssembler.toEntryDomainList(entryMapper.selectList(wrapper));
    }

    private List<Long> listVolumeIdsByCategory(SancaiCategoryId categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return volumeMapper
                .selectList(new LambdaQueryWrapper<SancaiVolumeDO>()
                        .eq(SancaiVolumeDO::getCategoryId, SancaiCategoryIdCodec.toValue(categoryId)))
                .stream()
                .map(SancaiVolumeDO::getId)
                .toList();
    }

    @Override
    public SancaiEntryId insertEntry(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        entryMapper.insert(dataObject);
        return SancaiEntryIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateEntry(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, dataObject.getId())
                        .set(SancaiEntryDO::getVolumeId, dataObject.getVolumeId())
                        .set(SancaiEntryDO::getTitle, dataObject.getTitle())
                        .set(SancaiEntryDO::getOriginalText, dataObject.getOriginalText())
                        .set(SancaiEntryDO::getTranslationText, dataObject.getTranslationText())
                        .set(SancaiEntryDO::getSummary, dataObject.getSummary())
                        .set(SancaiEntryDO::getLifecycleStatus, dataObject.getLifecycleStatus())
                        .set(SancaiEntryDO::getVisibility, dataObject.getVisibility())
                        .set(SancaiEntryDO::getTranslationStatus, dataObject.getTranslationStatus())
                        .set(SancaiEntryDO::getImageStatus, dataObject.getImageStatus())
                        .set(SancaiEntryDO::getVisualAssetStatus, dataObject.getVisualAssetStatus())
                        .set(SancaiEntryDO::getRefinementStatus, dataObject.getRefinementStatus())
                        .set(
                                dataObject.getCurrentVersionId() != null,
                                SancaiEntryDO::getCurrentVersionId,
                                dataObject.getCurrentVersionId())
                        .set(
                                dataObject.getCurrentVersionNo() != null,
                                SancaiEntryDO::getCurrentVersionNo,
                                dataObject.getCurrentVersionNo())
                        .set(
                                dataObject.getCurrentVersionedAt() != null,
                                SancaiEntryDO::getCurrentVersionedAt,
                                dataObject.getCurrentVersionedAt())
                        .set(SancaiEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateRestoredEntry(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, dataObject.getId())
                        .set(SancaiEntryDO::getVolumeId, dataObject.getVolumeId())
                        .set(SancaiEntryDO::getTitle, dataObject.getTitle())
                        .set(SancaiEntryDO::getOriginalText, dataObject.getOriginalText())
                        .set(SancaiEntryDO::getTranslationText, dataObject.getTranslationText())
                        .set(SancaiEntryDO::getSummary, dataObject.getSummary())
                        .set(SancaiEntryDO::getLifecycleStatus, dataObject.getLifecycleStatus())
                        .set(SancaiEntryDO::getVisibility, dataObject.getVisibility())
                        .set(SancaiEntryDO::getTranslationStatus, dataObject.getTranslationStatus())
                        .set(SancaiEntryDO::getImageStatus, dataObject.getImageStatus())
                        .set(SancaiEntryDO::getVisualAssetStatus, dataObject.getVisualAssetStatus())
                        .set(SancaiEntryDO::getRefinementStatus, dataObject.getRefinementStatus())
                        .set(SancaiEntryDO::getPriority, dataObject.getPriority())
                        .set(
                                dataObject.getCurrentVersionId() != null,
                                SancaiEntryDO::getCurrentVersionId,
                                dataObject.getCurrentVersionId())
                        .set(
                                dataObject.getCurrentVersionNo() != null,
                                SancaiEntryDO::getCurrentVersionNo,
                                dataObject.getCurrentVersionNo())
                        .set(
                                dataObject.getCurrentVersionedAt() != null,
                                SancaiEntryDO::getCurrentVersionedAt,
                                dataObject.getCurrentVersionedAt())
                        .set(SancaiEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateEntryStatus(SancaiEntry entry) {
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, SancaiEntryIdCodec.toValue(entry.getId()))
                        .set(
                                SancaiEntryDO::getLifecycleStatus,
                                entry.getLifecycleStatus() == null
                                        ? null
                                        : entry.getLifecycleStatus().value()));
    }

    @Override
    public int updateEntryVisibility(SancaiEntryId id, String visibility) {
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, SancaiEntryIdCodec.toValue(id))
                        .set(SancaiEntryDO::getVisibility, visibility));
    }

    @Override
    public int deleteEntryById(SancaiEntryId id) {
        return entryMapper.deleteById(SancaiEntryIdCodec.toValue(id));
    }

    @Override
    public int updateCategoryPriority(SancaiCategory category) {
        SancaiCategoryDO dataObject = SancaiPersistenceAssembler.toCategoryObject(category);
        return categoryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiCategoryDO>()
                        .eq(SancaiCategoryDO::getId, dataObject.getId())
                        .set(SancaiCategoryDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int updateVolumePriority(SancaiVolume volume) {
        SancaiVolumeDO dataObject = SancaiPersistenceAssembler.toVolumeObject(volume);
        return volumeMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVolumeDO>()
                        .eq(SancaiVolumeDO::getId, dataObject.getId())
                        .set(SancaiVolumeDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int updateEntryPriority(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, dataObject.getId())
                        .set(SancaiEntryDO::getPriority, dataObject.getPriority()));
    }

    private static int maxPriority(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        Object max = values.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static SancaiCategoryOverview toCategoryOverview(Map<String, Object> row) {
        return new SancaiCategoryOverview(
                SancaiCategoryIdCodec.toDomain(longValue(row, "categoryId", "categoryid", "category_id")),
                longValue(row, "publicEntryCount", "publicentrycount", "public_entry_count"),
                longValue(row, "illustratedEntryCount", "illustratedentrycount", "illustrated_entry_count"),
                SancaiEntryIdCodec.toDomain(
                        longValue(row, "representativeEntryId", "representativeentryid", "representative_entry_id")),
                SancaiEntryImageIdCodec.toDomain(
                        longValue(row, "representativeImageId", "representativeimageid", "representative_image_id")),
                stringValue(row, "representativeImageTitle", "representativeimagetitle", "representative_image_title"));
    }

    private static Long longValue(Map<String, Object> row, String... keys) {
        Object value = rowValue(row, keys);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private static String stringValue(Map<String, Object> row, String... keys) {
        Object value = rowValue(row, keys);
        return value == null ? null : String.valueOf(value);
    }

    private static Object rowValue(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
    }
}
