package com.thundax.kuzhambu.classics.infra.sancai.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
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
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SancaiAssetRepositoryImpl implements SancaiAssetRepository {

    private final SancaiEntryDraftMapper draftMapper;
    private final SancaiAssetMapper imageMapper;
    private final SancaiVisualAssetMapper visualAssetMapper;
    private final SancaiShowcaseMapper showcaseMapper;

    public SancaiAssetRepositoryImpl(SancaiEntryDraftMapper draftMapper, SancaiAssetMapper imageMapper, SancaiVisualAssetMapper visualAssetMapper, SancaiShowcaseMapper showcaseMapper) {
        this.draftMapper = draftMapper;
        this.imageMapper = imageMapper;
        this.visualAssetMapper = visualAssetMapper;
        this.showcaseMapper = showcaseMapper;
    }

    @Override
    public Long insertDraft(SancaiEntryDraft draft) {
        SancaiEntryDraftDO dataObject = SancaiAssetPersistenceAssembler.toDraftObject(draft);
        draftMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public SancaiEntryDraft getLatestDraftByEntryId(Long entryId) {
        LambdaQueryWrapper<SancaiEntryDraftDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SancaiEntryDraftDO::getEntryId, entryId).orderByDesc(SancaiEntryDraftDO::getAutosavedAt).last("limit 1");
        return SancaiAssetPersistenceAssembler.toDraftDomain(draftMapper.selectOne(wrapper));
    }

    @Override
    public int deleteDraftByEntryId(Long entryId) {
        return draftMapper.delete(new LambdaQueryWrapper<SancaiEntryDraftDO>().eq(SancaiEntryDraftDO::getEntryId, entryId));
    }

    @Override
    public Long insertImage(SancaiEntryImage image) {
        SancaiEntryImageDO dataObject = SancaiAssetPersistenceAssembler.toImageObject(image);
        imageMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateImage(SancaiEntryImage image) {
        return imageMapper.updateById(SancaiAssetPersistenceAssembler.toImageObject(image));
    }

    @Override
    public int deleteImageById(Long id) {
        return imageMapper.deleteById(id);
    }

    @Override
    public List<SancaiEntryImage> listImagesByEntryId(Long entryId, SortDirection sortDirection) {
        LambdaQueryWrapper<SancaiEntryImageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SancaiEntryImageDO::getEntryId, entryId).orderBy(true, sortDirection != SortDirection.DESC, SancaiEntryImageDO::getPriority);
        return SancaiAssetPersistenceAssembler.toImageDomainList(imageMapper.selectList(wrapper));
    }

    @Override
    public Long insertVisualAsset(SancaiVisualAsset visualAsset) {
        SancaiVisualAssetDO dataObject = SancaiAssetPersistenceAssembler.toVisualAssetObject(visualAsset);
        visualAssetMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateVisualAsset(SancaiVisualAsset visualAsset) {
        return visualAssetMapper.updateById(SancaiAssetPersistenceAssembler.toVisualAssetObject(visualAsset));
    }

    @Override
    public int updateCurrentVisualAsset(Long entryId, Long visualAssetId) {
        visualAssetMapper.update(null, new LambdaUpdateWrapper<SancaiVisualAssetDO>().eq(SancaiVisualAssetDO::getEntryId, entryId).set(SancaiVisualAssetDO::getCurrentUsed, false));
        return visualAssetMapper.update(null, new LambdaUpdateWrapper<SancaiVisualAssetDO>().eq(SancaiVisualAssetDO::getEntryId, entryId).eq(SancaiVisualAssetDO::getId, visualAssetId).set(SancaiVisualAssetDO::getCurrentUsed, true));
    }

    @Override
    public List<SancaiVisualAsset> listVisualAssetsByEntryId(Long entryId) {
        return SancaiAssetPersistenceAssembler.toVisualAssetDomainList(visualAssetMapper.selectList(new LambdaQueryWrapper<SancaiVisualAssetDO>().eq(SancaiVisualAssetDO::getEntryId, entryId).orderByDesc(SancaiVisualAssetDO::getVersionNo)));
    }

    @Override
    public Long insertShowcase(SancaiShowcase showcase) {
        SancaiShowcaseDO dataObject = SancaiAssetPersistenceAssembler.toShowcaseObject(showcase);
        showcaseMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateShowcase(SancaiShowcase showcase) {
        return showcaseMapper.updateById(SancaiAssetPersistenceAssembler.toShowcaseObject(showcase));
    }

    @Override
    public Page<SancaiShowcase> pageShowcases(String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<SancaiShowcaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(status), SancaiShowcaseDO::getStatus, status).orderByDesc(SancaiShowcaseDO::getRequestedAt);
        Page<SancaiShowcaseDO> dataPage = showcaseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<SancaiShowcase> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(SancaiAssetPersistenceAssembler.toShowcaseDomainList(dataPage.getRecords()));
        return entityPage;
    }
}
