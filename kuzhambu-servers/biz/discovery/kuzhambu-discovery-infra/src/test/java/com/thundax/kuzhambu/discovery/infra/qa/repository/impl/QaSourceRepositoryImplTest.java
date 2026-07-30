package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSourceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSourceMapper;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaSourceRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenSourceIdIsMissing() {
        QaSourceMapper mapper = mock(QaSourceMapper.class);
        QaSourceRepositoryImpl repository = new QaSourceRepositoryImpl(mapper);
        QaSource entity = new QaSource(
                null,
                null,
                "SANCAI:1001",
                2001L,
                "SANCAI_ENTRY",
                1001L,
                "SANCAI",
                "黄帝",
                "卷一",
                "上古帝王",
                "/knowledge/sancai/1001",
                1,
                new BigDecimal("0.98"),
                "CITED",
                new Date());
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaSourceDO.class).setId(6001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaSourceDO.class));

        Long savedId = repository.save(entity);

        assertEquals(6001L, savedId);
        verify(mapper).insert(any(QaSourceDO.class));
    }

    @Test
    void listByMessageIdShouldReturnRankedSources() {
        QaSourceMapper mapper = mock(QaSourceMapper.class);
        QaSourceRepositoryImpl repository = new QaSourceRepositoryImpl(mapper);
        QaSourceDO dataObject = new QaSourceDO(
                6001L,
                "SANCAI:1001",
                2001L,
                "SANCAI_ENTRY",
                1001L,
                "SANCAI",
                "黄帝",
                "卷一",
                "上古帝王",
                "/knowledge/sancai/1001",
                1,
                new BigDecimal("0.98"),
                "CITED",
                new Date());
        when(mapper.selectList(any())).thenReturn(List.of(dataObject));

        List<QaSource> result = repository.listByMessageId(2001L);

        assertEquals(1, result.size());
        assertEquals(6001L, result.get(0).getId());
        assertEquals("黄帝", result.get(0).getTitleSnapshot());
    }
}
