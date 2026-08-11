package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaOwnerRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionMapper;
import java.time.Instant;
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
                Instant.now(),
                Instant.now(),
                null);
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaSessionDO.class).setId(4001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaSessionDO.class));

        QaSessionId savedId = repository.save(entity);

        assertEquals(4001L, QaSessionIdCodec.toValue(savedId));
        verify(mapper).insert(any(QaSessionDO.class));
    }

    @Test
    void listByOwnerUserIdShouldReturnRecentSessions() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        QaSessionDO dataObject = new QaSessionDO(
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
                Instant.now(),
                Instant.now(),
                null);
        when(mapper.selectList(any())).thenReturn(List.of(dataObject));

        List<QaSession> result = repository.listByOwnerUserId(ownerRef("USER", "1001"), 10);

        assertEquals(1, result.size());
        assertEquals(4001L, QaSessionIdCodec.toValue(result.get(0).getId()));
        assertEquals("黄帝问答", result.get(0).getTitle());
        ArgumentCaptor<QueryWrapper<QaSessionDO>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("removed_at IS NULL"));
    }

    @Test
    void pageShouldPushFiltersSortingAndLimitToMapper() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        QaSessionDO dataObject = new QaSessionDO(
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
                Instant.ofEpochMilli(1_718_000_000_000L),
                Instant.ofEpochMilli(1_718_000_100_000L),
                null);
        Page<QaSessionDO> mapperPage = new Page<>(2, 20);
        mapperPage.setTotal(31);
        mapperPage.setRecords(List.of(dataObject));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(mapperPage);
        Instant openedAtStart = Instant.ofEpochMilli(1_718_000_000_000L);
        Instant openedAtEnd = Instant.ofEpochMilli(1_718_086_400_000L);

        PageResult<QaSession> result = repository.page("黄帝", openedAtStart, openedAtEnd, 2, 20);

        assertEquals(2, result.getPageNo());
        assertEquals(20, result.getPageSize());
        assertEquals(31, result.getTotalCount());
        assertEquals(4001L, QaSessionIdCodec.toValue(result.getRecords().get(0).getId()));
        ArgumentCaptor<Page<QaSessionDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<QueryWrapper<QaSessionDO>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(2, pageCaptor.getValue().getCurrent());
        assertEquals(20, pageCaptor.getValue().getSize());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("title LIKE"));
        assertTrue(sqlSegment.contains("opened_at >="));
        assertTrue(sqlSegment.contains("opened_at <="));
        assertFalse(sqlSegment.contains("removed_at IS NULL"));
        assertTrue(sqlSegment.contains("ORDER BY opened_at DESC,id DESC"));
    }

    @Test
    void deleteShouldDelegateConditionalUpdate() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        Instant removedAt = Instant.now();
        when(mapper.delete(4001L, removedAt)).thenReturn(1);

        int updated = repository.delete(QaSessionIdCodec.toDomain(4001L), removedAt);

        assertEquals(1, updated);
        verify(mapper).delete(eq(4001L), eq(removedAt));
    }

    @Test
    void deleteShouldReturnZeroWhenAlreadyRemoved() {
        QaSessionMapper mapper = mock(QaSessionMapper.class);
        QaSessionRepositoryImpl repository = new QaSessionRepositoryImpl(mapper);
        Instant removedAt = Instant.now();
        when(mapper.delete(4001L, removedAt)).thenReturn(0);

        int updated = repository.delete(QaSessionIdCodec.toDomain(4001L), removedAt);

        assertEquals(0, updated);
    }

    private QaOwnerRef ownerRef(String ownerType, String ownerId) {
        return QaStringValueCodec.toOwnerRef(ownerType, ownerId);
    }
}
