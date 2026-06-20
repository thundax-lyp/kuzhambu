package com.thundax.kuzhambu.classics.infra.mingcustoms.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsKeywordDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsMapper;
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
