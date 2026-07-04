package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickMapper;
import java.util.Date;
import org.junit.jupiter.api.Test;

class SearchClickRepositoryImplTest {

    @Test
    void countByCreatedAtRangeShouldDelegateMapper() {
        SearchClickMapper mapper = mock(SearchClickMapper.class);
        SearchClickRepositoryImpl repository = new SearchClickRepositoryImpl(mapper);
        Date createdAtStart = new Date(1_718_000_000_000L);
        Date createdAtEnd = new Date(1_720_419_200_000L);
        when(mapper.countByCreatedAtRange(createdAtStart, createdAtEnd)).thenReturn(3L);

        long count = repository.countByCreatedAtRange(createdAtStart, createdAtEnd);

        assertEquals(3L, count);
        verify(mapper).countByCreatedAtRange(createdAtStart, createdAtEnd);
    }

    @Test
    void countByCreatedAtRangeShouldReturnZeroWhenMapperReturnsNull() {
        SearchClickMapper mapper = mock(SearchClickMapper.class);
        SearchClickRepositoryImpl repository = new SearchClickRepositoryImpl(mapper);
        when(mapper.countByCreatedAtRange(null, null)).thenReturn(null);

        long count = repository.countByCreatedAtRange(null, null);

        assertEquals(0L, count);
    }
}
