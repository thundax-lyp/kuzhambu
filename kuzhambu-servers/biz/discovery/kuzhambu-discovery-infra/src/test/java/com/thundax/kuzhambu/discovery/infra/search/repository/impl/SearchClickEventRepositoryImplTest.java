package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickEventMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SearchClickEventRepositoryImplTest {

    @Test
    void countByCreatedAtRangeShouldDelegateMapper() {
        SearchClickEventMapper mapper = mock(SearchClickEventMapper.class);
        SearchClickEventRepositoryImpl repository = new SearchClickEventRepositoryImpl(mapper);
        Instant createdAtStart = Instant.ofEpochMilli(1_718_000_000_000L);
        Instant createdAtEnd = Instant.ofEpochMilli(1_720_419_200_000L);
        when(mapper.countByCreatedAtRange(createdAtStart, createdAtEnd)).thenReturn(3L);

        long count = repository.countByCreatedAtRange(createdAtStart, createdAtEnd);

        assertEquals(3L, count);
        verify(mapper).countByCreatedAtRange(createdAtStart, createdAtEnd);
    }

    @Test
    void countByCreatedAtRangeShouldReturnZeroWhenMapperReturnsNull() {
        SearchClickEventMapper mapper = mock(SearchClickEventMapper.class);
        SearchClickEventRepositoryImpl repository = new SearchClickEventRepositoryImpl(mapper);
        when(mapper.countByCreatedAtRange(null, null)).thenReturn(null);

        long count = repository.countByCreatedAtRange(null, null);

        assertEquals(0L, count);
    }
}
