package com.thundax.kuzhambu.classics.infra.mingcustoms.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsKeywordDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MingCustomsRepositoryTest {

    @Test
    void keywordCloudShouldMapAggregatedCounts() {
        MingCustomsMapper keywordMapper = mock(MingCustomsMapper.class);
        when(keywordMapper.selectMaps(any())).thenReturn(List.of(Map.of("keyword", "礼俗", "count", 3L)));
        MingCustomsRepositoryImpl repository =
                new MingCustomsRepositoryImpl(mock(MingCustomsEntryMapper.class), keywordMapper);

        List<MingCustomsKeywordCloudItem> items = repository.listKeywordCloud(null);

        assertEquals(1, items.size());
        assertEquals("礼俗", items.get(0).getKeyword());
        assertEquals(3L, items.get(0).getCount());
    }

    @Test
    void tagCloudShouldMapUnifiedContentTagAggregatedCounts() {
        MingCustomsEntryMapper entryMapper = mock(MingCustomsEntryMapper.class);
        when(entryMapper.selectTagCloud("礼俗", "祭祀", "PUBLIC"))
                .thenReturn(List.of(Map.of("tagId", 7001L, "tagNameSnapshot", "祭祀", "count", 3L)));
        MingCustomsRepositoryImpl repository =
                new MingCustomsRepositoryImpl(entryMapper, mock(MingCustomsMapper.class));

        List<MingCustomsTagCloudItem> items = repository.listTagCloud("礼俗", "祭祀", "PUBLIC");

        assertEquals(1, items.size());
        assertEquals(7001L, items.get(0).getTagId());
        assertEquals("祭祀", items.get(0).getTagNameSnapshot());
        assertEquals(3L, items.get(0).getCount());
        verify(entryMapper).selectTagCloud("礼俗", "祭祀", "PUBLIC");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldApplyUnifiedTagIdFilter() {
        MingCustomsEntryMapper entryMapper = mock(MingCustomsEntryMapper.class);
        when(entryMapper.selectPage(any(), any())).thenReturn(new Page<MingCustomsEntryDO>(1, 20, 0));
        MingCustomsRepositoryImpl repository =
                new MingCustomsRepositoryImpl(entryMapper, mock(MingCustomsMapper.class));

        repository.page(null, null, null, 7001L, null, "PUBLIC", SortDirection.ASC, 1, 20);

        ArgumentCaptor<LambdaQueryWrapper<MingCustomsEntryDO>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(entryMapper).selectPage(any(), captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("classics_content_tag"));
        assertTrue(captor.getValue().getSqlSegment().contains("tag.content_type = 'MING_CUSTOMS'"));
        assertTrue(captor.getValue().getSqlSegment().contains("tag.status = 'ACTIVE'"));
        assertTrue(captor.getValue().getSqlSegment().contains("tag.tag_id"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldApplyUnifiedTagNameSnapshotFilter() {
        MingCustomsEntryMapper entryMapper = mock(MingCustomsEntryMapper.class);
        when(entryMapper.selectPage(any(), any())).thenReturn(new Page<MingCustomsEntryDO>(1, 20, 0));
        MingCustomsRepositoryImpl repository =
                new MingCustomsRepositoryImpl(entryMapper, mock(MingCustomsMapper.class));

        repository.page(null, null, "旧关键词", null, "祭祀", "PUBLIC", SortDirection.ASC, 1, 20);

        ArgumentCaptor<LambdaQueryWrapper<MingCustomsEntryDO>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(entryMapper).selectPage(any(), captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("classics_content_tag"));
        assertTrue(captor.getValue().getSqlSegment().contains("tag.tag_name_snapshot"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void keywordCloudShouldApplyVisibilityFilter() {
        MingCustomsMapper keywordMapper = mock(MingCustomsMapper.class);
        when(keywordMapper.selectMaps(any())).thenReturn(List.of());
        MingCustomsRepositoryImpl repository =
                new MingCustomsRepositoryImpl(mock(MingCustomsEntryMapper.class), keywordMapper);

        repository.listKeywordCloud("PUBLIC");

        ArgumentCaptor<QueryWrapper<MingCustomsKeywordDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(keywordMapper).selectMaps(captor.capture());
        QueryWrapper<MingCustomsKeywordDO> wrapper = captor.getValue();
        assertEquals("keyword,count(*) as count", wrapper.getSqlSelect());
        assertTrue(wrapper.getSqlSegment().contains("GROUP BY keyword"));
        assertTrue(wrapper.getSqlSegment().contains("ORDER BY count DESC,keyword ASC"));
        assertTrue(wrapper.getSqlSegment().contains("classics_ming_customs_entry"));
        assertTrue(wrapper.getSqlSegment().contains("visibility"));
    }
}
