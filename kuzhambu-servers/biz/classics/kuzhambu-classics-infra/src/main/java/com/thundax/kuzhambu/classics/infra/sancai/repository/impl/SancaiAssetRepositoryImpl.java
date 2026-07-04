package com.thundax.kuzhambu.classics.infra.sancai.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryDraftIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiShowcaseIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiAssetPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDraftDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryImageDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiShowcaseDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiVisualAssetDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiAssetMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiEntryDraftMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiShowcaseMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiVisualAssetMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SancaiAssetRepositoryImpl implements SancaiAssetRepository {

    private final SancaiEntryDraftMapper draftMapper;
    private final SancaiAssetMapper imageMapper;
    private final SancaiVisualAssetMapper visualAssetMapper;
    private final SancaiShowcaseMapper showcaseMapper;

    public SancaiAssetRepositoryImpl(
            SancaiEntryDraftMapper draftMapper,
            SancaiAssetMapper imageMapper,
            SancaiVisualAssetMapper visualAssetMapper,
            SancaiShowcaseMapper showcaseMapper) {
        this.draftMapper = draftMapper;
        this.imageMapper = imageMapper;
        this.visualAssetMapper = visualAssetMapper;
        this.showcaseMapper = showcaseMapper;
    }

    @Override
    public SancaiEntryDraftId insertDraft(SancaiEntryDraft draft) {
        SancaiEntryDraftDO dataObject = SancaiAssetPersistenceAssembler.toDraftObject(draft);
        draftMapper.insert(dataObject);
        return SancaiEntryDraftIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public SancaiEntryDraft getLatestDraftByEntryId(SancaiEntryId entryId) {
        LambdaQueryWrapper<SancaiEntryDraftDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SancaiEntryDraftDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                .orderByDesc(SancaiEntryDraftDO::getAutosavedAt)
                .last("limit 1");
        return SancaiAssetPersistenceAssembler.toDraftDomain(draftMapper.selectOne(wrapper));
    }

    @Override
    public int deleteDraftByEntryId(SancaiEntryId entryId) {
        return draftMapper.delete(new LambdaQueryWrapper<SancaiEntryDraftDO>()
                .eq(SancaiEntryDraftDO::getEntryId, SancaiEntryIdCodec.toValue(entryId)));
    }

    @Override
    public SancaiEntryImageId insertImage(SancaiEntryImage image) {
        SancaiEntryImageDO dataObject = SancaiAssetPersistenceAssembler.toImageObject(image);
        imageMapper.insert(dataObject);
        return SancaiEntryImageIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateImage(SancaiEntryImage image) {
        SancaiEntryImageDO dataObject = SancaiAssetPersistenceAssembler.toImageObject(image);
        return imageMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryImageDO>()
                        .eq(SancaiEntryImageDO::getId, dataObject.getId())
                        .set(SancaiEntryImageDO::getEntryId, dataObject.getEntryId())
                        .set(SancaiEntryImageDO::getStorageObjectId, dataObject.getStorageObjectId())
                        .set(SancaiEntryImageDO::getImageType, dataObject.getImageType())
                        .set(SancaiEntryImageDO::getTitle, dataObject.getTitle())
                        .set(SancaiEntryImageDO::getCurrentUsed, dataObject.getCurrentUsed()));
    }

    @Override
    public int deleteImageById(SancaiEntryImageId id) {
        return imageMapper.deleteById(SancaiEntryImageIdCodec.toValue(id));
    }

    @Override
    public SancaiEntryImage getImageById(SancaiEntryImageId id) {
        return SancaiAssetPersistenceAssembler.toImageDomain(
                imageMapper.selectById(SancaiEntryImageIdCodec.toValue(id)));
    }

    @Override
    public List<SancaiEntryImage> listImages(SortDirection sortDirection) {
        return SancaiAssetPersistenceAssembler.toImageDomainList(
                imageMapper.selectList(new LambdaQueryWrapper<SancaiEntryImageDO>()
                        .orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryImageDO::getPriority)));
    }

    @Override
    public List<SancaiEntryImage> listImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiEntryImageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                .orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryImageDO::getPriority);
        return SancaiAssetPersistenceAssembler.toImageDomainList(imageMapper.selectList(wrapper));
    }

    @Override
    public List<SancaiEntryImage> listCurrentImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiEntryImageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                .eq(SancaiEntryImageDO::getCurrentUsed, true)
                .orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryImageDO::getPriority);
        return SancaiAssetPersistenceAssembler.toImageDomainList(imageMapper.selectList(wrapper));
    }

    @Override
    public int clearCurrentImagesByEntryId(SancaiEntryId entryId) {
        return imageMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryImageDO>()
                        .eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                        .set(SancaiEntryImageDO::getCurrentUsed, false));
    }

    @Override
    public int markImageCurrent(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        return imageMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryImageDO>()
                        .eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                        .eq(SancaiEntryImageDO::getId, SancaiEntryImageIdCodec.toValue(imageId))
                        .set(SancaiEntryImageDO::getCurrentUsed, true));
    }

    @Override
    public int maxPriority() {
        return maxPriority(imageMapper.selectObjs(new QueryWrapper<SancaiEntryImageDO>().select("max(priority)")));
    }

    @Override
    public int updatePriority(SancaiEntryImage image) {
        SancaiEntryImageDO dataObject = SancaiAssetPersistenceAssembler.toImageObject(image);
        return imageMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryImageDO>()
                        .eq(SancaiEntryImageDO::getId, dataObject.getId())
                        .set(SancaiEntryImageDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public SancaiVisualAssetId insertVisualAsset(SancaiVisualAsset visualAsset) {
        SancaiVisualAssetDO dataObject = SancaiAssetPersistenceAssembler.toVisualAssetObject(visualAsset);
        visualAssetMapper.insert(dataObject);
        return SancaiVisualAssetIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateVisualAsset(SancaiVisualAsset visualAsset) {
        return visualAssetMapper.updateById(SancaiAssetPersistenceAssembler.toVisualAssetObject(visualAsset));
    }

    @Override
    public SancaiVisualAsset getVisualAssetById(SancaiVisualAssetId visualAssetId) {
        return SancaiAssetPersistenceAssembler.toVisualAssetDomain(
                visualAssetMapper.selectById(SancaiVisualAssetIdCodec.toValue(visualAssetId)));
    }

    @Override
    public int updateVisualAssetImageAnalysisMarkdown(SancaiVisualAssetId visualAssetId, String imageAnalysisMarkdown) {
        return visualAssetMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getId, SancaiVisualAssetIdCodec.toValue(visualAssetId))
                        .set(SancaiVisualAssetDO::getImageAnalysisMarkdown, imageAnalysisMarkdown));
    }

    @Override
    public int updateVisualAssetFusionDescription(SancaiVisualAssetId visualAssetId, String fusionDescription) {
        return visualAssetMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getId, SancaiVisualAssetIdCodec.toValue(visualAssetId))
                        .set(SancaiVisualAssetDO::getFusionDescription, StringUtils.trimToNull(fusionDescription)));
    }

    @Override
    public int updateVisualAssetVisualDescription(SancaiVisualAssetId visualAssetId, String visualDescription) {
        return visualAssetMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getId, SancaiVisualAssetIdCodec.toValue(visualAssetId))
                        .set(SancaiVisualAssetDO::getVisualDescription, visualDescription));
    }

    @Override
    public int updateCurrentVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        visualAssetMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                        .set(SancaiVisualAssetDO::getCurrentUsed, false));
        return visualAssetMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                        .eq(SancaiVisualAssetDO::getId, SancaiVisualAssetIdCodec.toValue(visualAssetId))
                        .set(SancaiVisualAssetDO::getCurrentUsed, true));
    }

    @Override
    public List<SancaiVisualAsset> listVisualAssetsByEntryId(SancaiEntryId entryId) {
        return SancaiAssetPersistenceAssembler.toVisualAssetDomainList(
                visualAssetMapper.selectList(new LambdaQueryWrapper<SancaiVisualAssetDO>()
                        .eq(SancaiVisualAssetDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))
                        .orderByDesc(SancaiVisualAssetDO::getVersionNo)));
    }

    @Override
    public int maxVisualAssetVersionNo(SancaiEntryId entryId) {
        List<Object> values = visualAssetMapper.selectObjs(new QueryWrapper<SancaiVisualAssetDO>()
                .select("max(version_no)")
                .eq("entry_id", SancaiEntryIdCodec.toValue(entryId)));
        return maxPriority(values);
    }

    @Override
    public SancaiShowcaseId insertShowcase(SancaiShowcase showcase) {
        SancaiShowcaseDO dataObject = SancaiAssetPersistenceAssembler.toShowcaseObject(showcase);
        showcaseMapper.insert(dataObject);
        return SancaiShowcaseIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateShowcase(SancaiShowcase showcase) {
        return showcaseMapper.updateById(SancaiAssetPersistenceAssembler.toShowcaseObject(showcase));
    }

    @Override
    public int markShowcaseCompleted(SancaiShowcaseId id, StorageObjectId storageObjectId, int entryCount) {
        return showcaseMapper.markShowcaseCompleted(
                SancaiShowcaseIdCodec.toValue(id),
                SancaiShowcaseStatus.COMPLETED.value(),
                StorageObjectIdCodec.toValue(storageObjectId),
                entryCount);
    }

    @Override
    public int markShowcaseFailed(SancaiShowcaseId id) {
        return showcaseMapper.markShowcaseStatus(
                SancaiShowcaseIdCodec.toValue(id), SancaiShowcaseStatus.FAILED.value());
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<SancaiShowcaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(status), SancaiShowcaseDO::getStatus, status)
                .orderByDesc(SancaiShowcaseDO::getRequestedAt);
        Page<SancaiShowcaseDO> dataPage = showcaseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                SancaiAssetPersistenceAssembler.toShowcaseDomainList(dataPage.getRecords()));
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
}
