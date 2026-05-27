package com.thundax.kuzhambu.classics.infra.wangqi.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler.WangqiDocumentPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper.WangqiDocumentMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class WangqiDocumentRepositoryImpl implements WangqiDocumentRepository {

    private final WangqiDocumentMapper mapper;

    public WangqiDocumentRepositoryImpl(WangqiDocumentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WangqiDocument getById(Long id) {
        return WangqiDocumentPersistenceAssembler.toDomain(mapper.selectById(id));
    }

    @Override
    public Page<WangqiDocument> page(String keyword, String visibility, SortDirection sortDirection, int pageNo, int pageSize) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = buildWrapper(keyword, visibility, sortDirection);
        Page<WangqiDocumentDO> dataPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<WangqiDocument> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(WangqiDocumentPersistenceAssembler.toDomainList(dataPage.getRecords()));
        return entityPage;
    }

    @Override
    public List<WangqiDocument> listTimeline(String visibility, SortDirection sortDirection) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(visibility), WangqiDocumentDO::getVisibility, visibility)
                .orderBy(true, sortDirection != SortDirection.DESC, WangqiDocumentDO::getDocumentTime);
        return WangqiDocumentPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public Long insert(WangqiDocument document) {
        WangqiDocumentDO dataObject = WangqiDocumentPersistenceAssembler.toObject(document);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(WangqiDocument document) {
        return mapper.updateById(WangqiDocumentPersistenceAssembler.toObject(document));
    }

    @Override
    public int updateStorageObjectId(Long id, Long storageObjectId) {
        return mapper.update(null, new LambdaUpdateWrapper<WangqiDocumentDO>().eq(WangqiDocumentDO::getId, id).set(WangqiDocumentDO::getStorageObjectId, storageObjectId));
    }

    @Override
    public int updateVisibility(Long id, String visibility) {
        return mapper.update(null, new LambdaUpdateWrapper<WangqiDocumentDO>().eq(WangqiDocumentDO::getId, id).set(WangqiDocumentDO::getVisibility, visibility));
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    private static LambdaQueryWrapper<WangqiDocumentDO> buildWrapper(String keyword, String visibility, SortDirection sortDirection) {
        LambdaQueryWrapper<WangqiDocumentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(visibility), WangqiDocumentDO::getVisibility, visibility)
                .and(StringUtils.isNotBlank(keyword), item -> item.like(WangqiDocumentDO::getTitle, keyword).or().like(WangqiDocumentDO::getSummary, keyword).or().like(WangqiDocumentDO::getContent, keyword))
                .orderBy(true, sortDirection != SortDirection.DESC, WangqiDocumentDO::getDocumentTime);
        return wrapper;
    }
}
