package com.thundax.kuzhambu.classics.infra.mingcustoms.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler.MingCustomsPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsKeywordDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MingCustomsRepositoryImpl implements MingCustomsRepository {

    private final MingCustomsEntryMapper entryMapper;
    private final MingCustomsMapper keywordMapper;

    public MingCustomsRepositoryImpl(MingCustomsEntryMapper entryMapper, MingCustomsMapper keywordMapper) {
        this.entryMapper = entryMapper;
        this.keywordMapper = keywordMapper;
    }

    @Override
    public MingCustomsEntry getById(Long id) {
        return MingCustomsPersistenceAssembler.toEntryDomain(entryMapper.selectById(id));
    }

    @Override
    public Page<MingCustomsEntry> page(String category, String keyword, String tagName, String visibility, SortDirection sortDirection, int pageNo, int pageSize) {
        LambdaQueryWrapper<MingCustomsEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(category), MingCustomsEntryDO::getCategory, category)
                .eq(StringUtils.isNotBlank(visibility), MingCustomsEntryDO::getVisibility, visibility)
                .and(StringUtils.isNotBlank(keyword), item -> item.like(MingCustomsEntryDO::getTitle, keyword).or().like(MingCustomsEntryDO::getSummary, keyword).or().like(MingCustomsEntryDO::getContent, keyword).or().like(MingCustomsEntryDO::getOriginalExcerpts, keyword))
                .orderBy(true, sortDirection != SortDirection.DESC, MingCustomsEntryDO::getId);
        Page<MingCustomsEntryDO> dataPage = entryMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<MingCustomsEntry> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(MingCustomsPersistenceAssembler.toEntryDomainList(dataPage.getRecords()));
        return entityPage;
    }

    @Override
    public Long insert(MingCustomsEntry entry) {
        MingCustomsEntryDO dataObject = MingCustomsPersistenceAssembler.toEntryObject(entry);
        entryMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(MingCustomsEntry entry) {
        return entryMapper.updateById(MingCustomsPersistenceAssembler.toEntryObject(entry));
    }

    @Override
    public int updateVisibility(Long id, String visibility) {
        return entryMapper.update(null, new LambdaUpdateWrapper<MingCustomsEntryDO>().eq(MingCustomsEntryDO::getId, id).set(MingCustomsEntryDO::getVisibility, visibility));
    }

    @Override
    public int deleteById(Long id) {
        return entryMapper.deleteById(id);
    }

    @Override
    public List<MingCustomsKeyword> listKeywordsByCustomId(Long customId, SortDirection sortDirection) {
        return MingCustomsPersistenceAssembler.toKeywordDomainList(keywordMapper.selectList(new LambdaQueryWrapper<MingCustomsKeywordDO>().eq(MingCustomsKeywordDO::getCustomId, customId).orderBy(true, sortDirection != SortDirection.DESC, MingCustomsKeywordDO::getPriority)));
    }

    @Override
    public Long insertKeyword(MingCustomsKeyword keyword) {
        MingCustomsKeywordDO dataObject = MingCustomsPersistenceAssembler.toKeywordObject(keyword);
        keywordMapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int deleteKeywordById(Long id) {
        return keywordMapper.deleteById(id);
    }

    @Override
    public List<String> listKeywordCloud(String visibility) {
        return keywordMapper.selectObjs(Wrappers.<MingCustomsKeywordDO>query().select("keyword")).stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }
}
