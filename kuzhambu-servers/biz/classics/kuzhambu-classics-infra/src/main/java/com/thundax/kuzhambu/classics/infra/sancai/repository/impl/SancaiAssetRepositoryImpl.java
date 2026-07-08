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
import java.util.Date;
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
    public List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {
        return draftMapper
                .selectList(new LambdaQueryWrapper<SancaiEntryDraftDO>()
                        .select(SancaiEntryDraftDO::getId)
                        .lt(SancaiEntryDraftDO::getAutosavedAt, cutoff)
                        .orderByAsc(SancaiEntryDraftDO::getAutosavedAt)
                        .last("limit " + limit))
                .stream()
                .map(SancaiEntryDraftDO::getId)
                .map(SancaiEntryDraftIdCodec::toDomain)
                .toList();
    }

    @Override
    public int deleteDraftById(SancaiEntryDraftId id) {
        return draftMapper.deleteById(SancaiEntryDraftIdCodec.toValue(id));
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
        return markShowcaseCompleted(id, storageObjectId, entryCount, 0, null, null, null, null);
    }

    @Override
    public int markShowcaseCompleted(
            SancaiShowcaseId id,
            StorageObjectId storageObjectId,
            int entryCount,
            int assetCount,
            String filename,
            String contentType,
            Long sizeBytes,
            String sha256) {
        return showcaseMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiShowcaseDO>()
                        .eq(SancaiShowcaseDO::getId, SancaiShowcaseIdCodec.toValue(id))
                        .set(SancaiShowcaseDO::getStatus, SancaiShowcaseStatus.COMPLETED.value())
                        .set(SancaiShowcaseDO::getCompletedAt, new Date())
                        .set(SancaiShowcaseDO::getStorageObjectId, StorageObjectIdCodec.toValue(storageObjectId))
                        .set(SancaiShowcaseDO::getEntryCount, entryCount)
                        .set(SancaiShowcaseDO::getAssetCount, assetCount)
                        .set(SancaiShowcaseDO::getFilename, filename)
                        .set(SancaiShowcaseDO::getContentType, contentType)
                        .set(SancaiShowcaseDO::getSizeBytes, sizeBytes)
                        .set(SancaiShowcaseDO::getSha256, sha256)
                        .set(SancaiShowcaseDO::getFailureType, null)
                        .set(SancaiShowcaseDO::getFailureMessage, null));
    }

    @Override
    public int markShowcaseFailed(SancaiShowcaseId id) {
        return markShowcaseFailed(id, null, null);
    }

    @Override
    public int markShowcaseFailed(SancaiShowcaseId id, String failureType, String failureMessage) {
        return showcaseMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiShowcaseDO>()
                        .eq(SancaiShowcaseDO::getId, SancaiShowcaseIdCodec.toValue(id))
                        .set(SancaiShowcaseDO::getStatus, SancaiShowcaseStatus.FAILED.value())
                        .set(SancaiShowcaseDO::getCompletedAt, new Date())
                        .set(SancaiShowcaseDO::getFailureType, failureType)
                        .set(SancaiShowcaseDO::getFailureMessage, failureMessage));
    }

    @Override
    public int markShowcaseExpired(SancaiShowcaseId id) {
        return showcaseMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiShowcaseDO>()
                        .eq(SancaiShowcaseDO::getId, SancaiShowcaseIdCodec.toValue(id))
                        .set(SancaiShowcaseDO::getStatus, SancaiShowcaseStatus.EXPIRED.value()));
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize) {
        return pageShowcases(null, status, null, null, null, pageNo, pageSize);
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(
            String keyword,
            String status,
            String visibilityRiskStatus,
            Date requestedAtStart,
            Date requestedAtEnd,
            int pageNo,
            int pageSize) {
        LambdaQueryWrapper<SancaiShowcaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(status), SancaiShowcaseDO::getStatus, status)
                .eq(
                        StringUtils.isNotBlank(visibilityRiskStatus),
                        SancaiShowcaseDO::getVisibilityRiskStatus,
                        visibilityRiskStatus)
                .ge(requestedAtStart != null, SancaiShowcaseDO::getRequestedAt, requestedAtStart)
                .le(requestedAtEnd != null, SancaiShowcaseDO::getRequestedAt, requestedAtEnd)
                .orderByDesc(SancaiShowcaseDO::getRequestedAt);
        appendKeyword(wrapper, keyword);
        Page<SancaiShowcaseDO> dataPage = showcaseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                SancaiAssetPersistenceAssembler.toShowcaseDomainList(dataPage.getRecords()));
    }

    private static void appendKeyword(LambdaQueryWrapper<SancaiShowcaseDO> wrapper, String keyword) {
        String normalizedKeyword = StringUtils.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return;
        }
        Long idKeyword = parseLong(normalizedKeyword);
        wrapper.and(condition -> {
            if (idKeyword != null) {
                condition.eq(SancaiShowcaseDO::getId, idKeyword).or();
            }
            condition
                    .like(SancaiShowcaseDO::getScopeTitle, normalizedKeyword)
                    .or()
                    .like(SancaiShowcaseDO::getFilename, normalizedKeyword);
        });
    }

    private static Long parseLong(String value) {
        if (!StringUtils.isNumeric(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
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
