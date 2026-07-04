package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QaSessionRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenSessionIdIsMissing() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        QaSession entity = new QaSession(
                null,
                null,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "SEARCH",
                null,
                null,
                "OPEN",
                new Date(),
                new Date(),
                null);

        Long savedId = repository.save(entity);

        assertNotNull(savedId);
        verify(mapper).insert(any(QaSessionDO.class));
    }

    @Test
    void listByOwnerUserIdShouldReturnRecentSessions() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        QaSessionDO dataObject = new QaSessionDO(
                1L,
                4001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "SEARCH",
                null,
                null,
                "OPEN",
                new Date(),
                new Date(),
                null);
        when(mapper.selectList(any())).thenReturn(List.of(dataObject));

        List<QaSession> result = repository.listByOwnerUserId("USER", "1001", 10);

        assertEquals(1, result.size());
        assertEquals(4001L, result.get(0).getSessionId());
        assertEquals("黄帝问答", result.get(0).getTitle());
        ArgumentCaptor<QueryWrapper<QaSessionDO>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("removed_at IS NULL"));
    }

    @Test
    void markRemovedShouldDelegateConditionalUpdate() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        Date removedAt = new Date();
        when(mapper.markRemoved(4001L, removedAt)).thenReturn(1);

        int updated = repository.markRemoved(4001L, removedAt);

        assertEquals(1, updated);
        verify(mapper).markRemoved(eq(4001L), eq(removedAt));
    }

    @Test
    void markRemovedShouldReturnZeroWhenAlreadyRemoved() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        Date removedAt = new Date();
        when(mapper.markRemoved(4001L, removedAt)).thenReturn(0);

        int updated = repository.markRemoved(4001L, removedAt);

        assertEquals(0, updated);
    }
}
