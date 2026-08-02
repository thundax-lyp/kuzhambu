package com.thundax.kuzhambu.classics.infra.mingcustoms.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler.MingCustomsPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsKeywordDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public MingCustomsEntry getById(MingCustomsEntryId id) {
        return MingCustomsPersistenceAssembler.toEntryDomain(
                entryMapper.selectById(MingCustomsEntryIdCodec.toValue(id)));
    }

    @Override
    public PageResult<MingCustomsEntry> page(
            String category,
            String keyword,
            String tagName,
            Long tagId,
            String tagNameSnapshot,
            SortDirection sortDirection,
            int pageNo,
            int pageSize) {
        LambdaQueryWrapper<MingCustomsEntryDO> wrapper =
                entryWrapper(category, keyword, tagName, tagId, tagNameSnapshot, sortDirection);
        Page<MingCustomsEntryDO> dataPage = entryMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                MingCustomsPersistenceAssembler.toEntryDomainList(dataPage.getRecords()));
    }

    @Override
    public List<MingCustomsEntry> list(
            String category,
            String keyword,
            String tagName,
            Long tagId,
            String tagNameSnapshot,
            SortDirection sortDirection) {
        return MingCustomsPersistenceAssembler.toEntryDomainList(entryMapper.selectList(
                entryWrapper(category, keyword, tagName, tagId, tagNameSnapshot, sortDirection)));
    }

    @Override
    public MingCustomsEntryId insert(MingCustomsEntry entry) {
        MingCustomsEntryDO dataObject = MingCustomsPersistenceAssembler.toEntryObject(entry);
        entryMapper.insert(dataObject);
        return MingCustomsEntryIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MingCustomsEntry entry) {
        MingCustomsEntryDO dataObject = MingCustomsPersistenceAssembler.toEntryObject(entry);
        return entryMapper.update(
                null,
                new LambdaUpdateWrapper<MingCustomsEntryDO>()
                        .eq(MingCustomsEntryDO::getId, dataObject.getId())
                        .set(MingCustomsEntryDO::getTitle, dataObject.getTitle())
                        .set(MingCustomsEntryDO::getCategory, dataObject.getCategory())
                        .set(MingCustomsEntryDO::getChapter, dataObject.getChapter())
                        .set(MingCustomsEntryDO::getSection, dataObject.getSection())
                        .set(MingCustomsEntryDO::getSummary, dataObject.getSummary())
                        .set(MingCustomsEntryDO::getContentFormat, dataObject.getContentFormat())
                        .set(MingCustomsEntryDO::getContent, dataObject.getContent())
                        .set(MingCustomsEntryDO::getOriginalExcerpts, dataObject.getOriginalExcerpts())
                        .set(
                                dataObject.getCurrentVersionId() != null,
                                MingCustomsEntryDO::getCurrentVersionId,
                                dataObject.getCurrentVersionId())
                        .set(
                                dataObject.getCurrentVersionNo() != null,
                                MingCustomsEntryDO::getCurrentVersionNo,
                                dataObject.getCurrentVersionNo())
                        .set(
                                dataObject.getCurrentVersionedAt() != null,
                                MingCustomsEntryDO::getCurrentVersionedAt,
                                dataObject.getCurrentVersionedAt())
                        .set(MingCustomsEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int deleteById(MingCustomsEntryId id) {
        return entryMapper.deleteById(MingCustomsEntryIdCodec.toValue(id));
    }

    @Override
    public List<MingCustomsKeyword> listKeywordsByCustomId(MingCustomsEntryId customId, SortDirection sortDirection) {
        return MingCustomsPersistenceAssembler.toKeywordDomainList(keywordMapper.selectList(new LambdaQueryWrapper<
                        MingCustomsKeywordDO>()
                .eq(customId != null, MingCustomsKeywordDO::getCustomId, MingCustomsEntryIdCodec.toValue(customId))
                .orderBy(true, sortDirection != SortDirection.DESC, MingCustomsKeywordDO::getPriority)));
    }

    @Override
    public List<MingCustomsKeyword> listKeywords(SortDirection sortDirection) {
        return MingCustomsPersistenceAssembler.toKeywordDomainList(
                keywordMapper.selectList(new LambdaQueryWrapper<MingCustomsKeywordDO>()
                        .orderBy(true, sortDirection != SortDirection.DESC, MingCustomsKeywordDO::getPriority)));
    }

    @Override
    public int maxPriority() {
        return maxPriority(keywordMapper.selectObjs(new QueryWrapper<MingCustomsKeywordDO>().select("max(priority)")));
    }

    @Override
    public MingCustomsKeywordId insertKeyword(MingCustomsKeyword keyword) {
        MingCustomsKeywordDO dataObject = MingCustomsPersistenceAssembler.toKeywordObject(keyword);
        keywordMapper.insert(dataObject);
        return MingCustomsKeywordIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateKeywordPriority(MingCustomsKeyword keyword) {
        MingCustomsKeywordDO dataObject = MingCustomsPersistenceAssembler.toKeywordObject(keyword);
        return keywordMapper.update(
                null,
                new LambdaUpdateWrapper<MingCustomsKeywordDO>()
                        .eq(MingCustomsKeywordDO::getId, dataObject.getId())
                        .set(MingCustomsKeywordDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int deleteKeywordById(MingCustomsKeywordId id) {
        return keywordMapper.deleteById(MingCustomsKeywordIdCodec.toValue(id));
    }

    @Override
    public List<MingCustomsKeywordCloudItem> listKeywordCloud() {
        QueryWrapper<MingCustomsKeywordDO> wrapper = Wrappers.<MingCustomsKeywordDO>query()
                .select("keyword", "count(*) as count")
                .groupBy("keyword")
                .orderByDesc("count")
                .orderByAsc("keyword");
        return keywordMapper.selectMaps(wrapper).stream()
                .map(MingCustomsRepositoryImpl::toKeywordCloudItem)
                .toList();
    }

    @Override
    public List<MingCustomsTagCloudItem> listTagCloud(String category, String keyword) {
        return entryMapper.selectTagCloud(category, keyword).stream()
                .map(MingCustomsRepositoryImpl::toTagCloudItem)
                .toList();
    }

    private static MingCustomsKeywordCloudItem toKeywordCloudItem(Map<String, Object> row) {
        return new MingCustomsKeywordCloudItem(String.valueOf(row.get("keyword")), toLong(row.get("count")));
    }

    private static MingCustomsTagCloudItem toTagCloudItem(Map<String, Object> row) {
        return new MingCustomsTagCloudItem(
                nullableLong(row.get("tagId")), String.valueOf(row.get("tagNameSnapshot")), toLong(row.get("count")));
    }

    private LambdaQueryWrapper<MingCustomsEntryDO> entryWrapper(
            String category,
            String keyword,
            String tagName,
            Long tagId,
            String tagNameSnapshot,
            SortDirection sortDirection) {
        LambdaQueryWrapper<MingCustomsEntryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(category), MingCustomsEntryDO::getCategory, category)
                .and(StringUtils.isNotBlank(keyword), item -> item.like(MingCustomsEntryDO::getTitle, keyword)
                        .or()
                        .like(MingCustomsEntryDO::getSummary, keyword)
                        .or()
                        .like(MingCustomsEntryDO::getContent, keyword)
                        .or()
                        .like(MingCustomsEntryDO::getOriginalExcerpts, keyword))
                .orderBy(true, sortDirection != SortDirection.DESC, MingCustomsEntryDO::getId);
        applyTagFilter(wrapper, tagName, tagId, tagNameSnapshot);
        return wrapper;
    }

    private void applyTagFilter(
            LambdaQueryWrapper<MingCustomsEntryDO> wrapper, String tagName, Long tagId, String tagNameSnapshot) {
        if (tagId != null) {
            wrapper.exists(
                    "select 1 from classics_content_tag tag"
                            + " where tag.content_type = 'MING_CUSTOMS'"
                            + " and tag.status = 'ACTIVE'"
                            + " and tag.content_id = id"
                            + " and tag.tag_id = {0}",
                    tagId);
            return;
        }
        String effectiveTagName = StringUtils.defaultIfBlank(tagNameSnapshot, tagName);
        if (StringUtils.isNotBlank(effectiveTagName)) {
            wrapper.exists(
                    "select 1 from classics_content_tag tag"
                            + " where tag.content_type = 'MING_CUSTOMS'"
                            + " and tag.status = 'ACTIVE'"
                            + " and tag.content_id = id"
                            + " and tag.tag_name_snapshot = {0}",
                    effectiveTagName);
        }
    }

    private static Long nullableLong(Object value) {
        return value == null ? null : toLong(value);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
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
